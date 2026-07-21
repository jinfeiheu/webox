package com.webox.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webox.common.enums.Allergen;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import com.webox.order.OrderRepository;
import com.webox.preference.PreferenceRepository;
import com.webox.preference.UserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI recommendation (PRD §5.2). Queries an OpenAI-compatible LLM with streaming,
 * asks for JSONL output, and emits each parsed recommendation as an SSE event
 * while the model is still generating — giving true streaming UX to the browser.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final DishRepository dishRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final OrderRepository orderRepository;
    private final PreferenceRepository preferenceRepository;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AiService(DishRepository dishRepository, DailyMenuRepository dailyMenuRepository,
                     OrderRepository orderRepository, PreferenceRepository preferenceRepository,
                     @Value("${app.llm.base-url}") String baseUrl,
                     @Value("${app.llm.api-key}") String apiKey,
                     @Value("${app.llm.model}") String model) {
        this.dishRepository = dishRepository;
        this.dailyMenuRepository = dailyMenuRepository;
        this.orderRepository = orderRepository;
        this.preferenceRepository = preferenceRepository;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public SseEmitter recommend(Long userId, String query) {
        SseEmitter emitter = new SseEmitter(90_000L); // 90 s timeout

        CompletableFuture.runAsync(() -> {
            try {
                doRecommend(userId, query, emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("AI recommendation failed", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private void doRecommend(Long userId, String query, SseEmitter emitter) throws Exception {
        if (apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
            emitter.send(SseEmitter.event().name("error")
                    .data("AI recommendation is not configured. Set the LLM_API_KEY environment variable (see README)."));
            return;
        }

        // ---- candidate filter (PRD §5.2) ----
        LocalDate today = LocalDate.now();
        List<Long> recentDishIds = orderRepository.findDishIdsOrderedByUserSince(
                userId, today.minusDays(7));

        var allergySet = preferenceRepository.findById(userId)
                .map(UserPreference::getAllergens)
                .map(java.util.HashSet::new)
                .orElseGet(java.util.HashSet::new);
        var pref = preferenceRepository.findById(userId);

        // active dishes, on today's menu, stock>0, no user allergens, not in recent 7-day history
        List<Dish> candidates = dailyMenuRepository.findByMenuDate(today).stream()
                .filter(dm -> dm.getStockRemaining() > 0)
                .map(dm -> dishRepository.findById(dm.getDish().getId()).orElse(null))
                .filter(d -> d != null && d.isActive())
                .filter(d -> !recentDishIds.contains(d.getId()))
                .filter(d -> d.getAllergens().stream().noneMatch(allergySet::contains))
                .toList();

        if (candidates.isEmpty()) {
            emitter.send(SseEmitter.event().name("error")
                    .data("No dishes match your criteria today. Try broadening your preferences."));
            return;
        }

        // ---- build prompt ----
        StringBuilder dishesJson = new StringBuilder("[");
        for (int i = 0; i < candidates.size(); i++) {
            Dish d = candidates.get(i);
            dishesJson.append(String.format(
                    "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"protein\":\"%s\","
                            + "\"spice\":\"%s\",\"price\":%.2f,\"desc\":\"%s\"}",
                    d.getId(), esc(d.getName()), d.getCategory().getLabel(),
                    d.getProtein(), d.getSpiceLevel().getLabel(), d.getPrice(),
                    esc(d.getDescription())));
            if (i < candidates.size() - 1) dishesJson.append(",");
        }
        dishesJson.append("]");

        String userPrefText = pref.isEmpty() ? "No preferences set."
                : String.format("allergens=%s, cuisines=%s, spice=%s, taste=%s",
                pref.get().getAllergens(), pref.get().getCuisines(),
                pref.get().getSpiceLevel(), pref.get().getTaste());

        String prompt = String.format(
                "You are WeBox cafeteria's meal recommender.\n"
                        + "The employee said: \"%s\"\n"
                        + "Their preferences: %s\n"
                        + "Available dishes today (only recommend from this list):\n%s\n\n"
                        + "Recommend at most 5 dishes. Output one JSON object per line (JSONL), no outer array:\n"
                        + "{\"dishId\":<id>,\"reason\":\"<one English sentence>\"}",
                query, userPrefText, dishesJson);

        // ---- call LLM with streaming ----
        var requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", true,
                "temperature", 0.7));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(80))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<java.io.InputStream> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        // Parse OpenAI SSE, accumulate content, emit completed JSONL lines.
        try (var reader = new BufferedReader(new InputStreamReader(response.body()))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ") || line.startsWith("data: [DONE]")) continue;
                String json = line.substring(6);
                try {
                    JsonNode node = objectMapper.readTree(json);
                    JsonNode delta = node.at("/choices/0/delta/content");
                    if (delta.isMissingNode()) continue;
                    buffer.append(delta.asText());
                    // Emit any complete lines (JSONL).
                    int nl;
                    while ((nl = buffer.indexOf("\n")) >= 0) {
                        String oneLine = buffer.substring(0, nl).trim();
                        buffer.delete(0, nl + 1);
                        if (oneLine.isEmpty()) continue;
                        try {
                            JsonNode item = objectMapper.readTree(oneLine);
                            long dishId = item.get("dishId").asLong();
                            String reason = item.get("reason").asText();
                            Dish dish = dishRepository.findById(dishId).orElse(null);
                            if (dish != null) {
                                emitter.send(SseEmitter.event().name("item").data(Map.of(
                                        "dishId", dishId,
                                        "dishName", dish.getName(),
                                        "imageUrl", dish.getImageUrl(),
                                        "category", dish.getCategory().getLabel(),
                                        "price", dish.getPrice(),
                                        "reason", reason)));
                            }
                        } catch (Exception parseErr) {
                            log.warn("Skipping unparseable AI line: {}", oneLine);
                        }
                    }
                } catch (Exception ignored) {
                    // occasional non-JSON data line
                }
            }
            // Emit any remaining text in buffer (model finished mid-line — parse as whole JSON object)
            String leftover = buffer.toString().trim();
            if (!leftover.isEmpty()) {
                try {
                    JsonNode item = objectMapper.readTree(leftover);
                    long dishId = item.get("dishId").asLong();
                    Dish dish = dishRepository.findById(dishId).orElse(null);
                    if (dish != null) {
                        emitter.send(SseEmitter.event().name("item").data(Map.of(
                                "dishId", dishId, "dishName", dish.getName(),
                                "imageUrl", dish.getImageUrl(), "category", dish.getCategory().getLabel(),
                                "price", dish.getPrice(), "reason", item.get("reason").asText())));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        emitter.send(SseEmitter.event().name("done").data(""));
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
