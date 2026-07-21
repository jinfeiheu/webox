package com.webox.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** PUT /api/admin/menus/{date} body — which dishes are on the menu and their day's supply. */
public record DailyMenuSetupRequest(@Valid List<Entry> entries) {

    public record Entry(
            @NotNull Long dishId,
            boolean selected,
            @Min(value = 0, message = "Stock cannot be negative.")
            int stockTotal) {
    }
}
