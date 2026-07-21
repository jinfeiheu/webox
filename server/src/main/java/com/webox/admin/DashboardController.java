package com.webox.admin;

import com.webox.admin.dto.DashboardView;
import com.webox.auth.RequireRole;
import com.webox.common.enums.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequireRole(Role.ADMIN)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard() {
        return dashboardService.getDashboard();
    }
}
