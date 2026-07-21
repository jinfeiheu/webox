package com.webox.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * In-process cache for hot menu data (PRD §1: 9:30-10:00 peak, high-frequency menu reads).
 * Writes (dish/stock changes) evict explicitly; the 10-minute TTL is only a safety net.
 */
@Configuration
public class CacheConfig {

    public static final String MENU_ITEMS = "menuItems";
    public static final String DISH_DETAIL = "dishDetail";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10)));
        return manager;
    }
}
