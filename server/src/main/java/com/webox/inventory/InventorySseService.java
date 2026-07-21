package com.webox.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events bus for real-time inventory changes (PRD §5.1).
 * One emitter per connected browser tab; events are broadcast to all on every stock mutation.
 */
@Component
public class InventorySseService {

    private static final Logger log = LoggerFactory.getLogger(InventorySseService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        // Fire an empty connected event so the frontend knows the stream is live.
        try {
            emitter.send(SseEmitter.event().name("connected").data(System.currentTimeMillis()));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void publish(InventoryEvent event) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("stock")
                        .data(event, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
