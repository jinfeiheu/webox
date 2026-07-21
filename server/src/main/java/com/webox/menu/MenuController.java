package com.webox.menu;

import com.webox.common.enums.Category;
import com.webox.menu.dto.DishDetailView;
import com.webox.menu.dto.MenuResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    public MenuResponse menu(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return menuService.getMenu(date == null ? LocalDate.now() : date);
    }

    @GetMapping("/dishes/search")
    public MenuResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        return menuService.search(effectiveDate, q, parseCategories(categories));
    }

    @GetMapping("/dishes/{id}")
    public DishDetailView dishDetail(@PathVariable("id") long dishId) {
        return menuService.getDishDetail(dishId);
    }

    /** Comma-separated English labels or enum names, e.g. "Chinese,Japanese" (invalid -> 400). */
    private static List<Category> parseCategories(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Category::from)
                .distinct()
                .toList();
    }
}
