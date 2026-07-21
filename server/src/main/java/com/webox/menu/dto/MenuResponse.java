package com.webox.menu.dto;

import java.time.LocalDate;
import java.util.List;

public record MenuResponse(LocalDate date, List<MenuItemView> items) {
}
