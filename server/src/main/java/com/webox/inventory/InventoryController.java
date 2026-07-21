package com.webox.inventory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE endpoint — authenticated via AuthInterceptor (token can be passed as query param). */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventorySseService sseService;

    public InventoryController(InventorySseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseService.subscribe();
    }
}
