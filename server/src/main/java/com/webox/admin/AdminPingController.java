package com.webox.admin;

import com.webox.auth.RequireRole;
import com.webox.common.enums.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Smoke endpoint proving Console APIs reject non-admin callers (PRD §4.3). */
@RestController
@RequestMapping("/api/admin")
@RequireRole(Role.ADMIN)
public class AdminPingController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "pong");
    }
}
