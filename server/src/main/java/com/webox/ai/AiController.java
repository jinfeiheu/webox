package com.webox.ai;

import com.webox.auth.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** AI recommendation — SSE streaming to the browser (PRD §5.2). Token via query param for EventSource. */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recommend")
    public SseEmitter recommend(@RequestParam("query") String query) {
        return aiService.recommend(AuthContext.require().id(), query.trim());
    }
}
