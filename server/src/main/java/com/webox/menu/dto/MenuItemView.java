package com.webox.menu.dto;

import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.menu.DailyMenu;
import com.webox.menu.Dish;

import java.math.BigDecimal;
import java.util.List;

/** Flat card view of a dish on a day's menu (no option groups — those load in the detail view). */
public record MenuItemView(
        Long dishId,
        String name,
        String description,
        BigDecimal price,
        Category category,
        String protein,
        SpiceLevel spiceLevel,
        List<Allergen> allergens,
        String imageUrl,
        int stockRemaining) {

    public static MenuItemView of(DailyMenu dailyMenu) {
        Dish dish = dailyMenu.getDish();
        return new MenuItemView(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getCategory(),
                dish.getProtein(),
                dish.getSpiceLevel(),
                List.copyOf(dish.getAllergens()),
                dish.getImageUrl(),
                dailyMenu.getStockRemaining());
    }
}
