package com.webox.admin;

import com.webox.admin.dto.DailyMenuAdminView;
import com.webox.admin.dto.DailyMenuSetupRequest;
import com.webox.auth.RequireRole;
import com.webox.common.enums.Role;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Console daily-menu setup (PRD §4.3) — ADMIN only. */
@RestController
@RequestMapping("/api/admin/menus")
@RequireRole(Role.ADMIN)
public class MenuAdminController {

    private final MenuAdminService menuAdminService;

    public MenuAdminController(MenuAdminService menuAdminService) {
        this.menuAdminService = menuAdminService;
    }

    @GetMapping("/{date}")
    public DailyMenuAdminView get(@PathVariable("date")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return menuAdminService.getMenuAdmin(date);
    }

    @PutMapping("/{date}")
    public void set(@PathVariable("date")
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                    @Valid @RequestBody DailyMenuSetupRequest request) {
        menuAdminService.setMenu(date, request);
    }
}
