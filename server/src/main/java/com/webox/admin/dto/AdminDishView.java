package com.webox.admin.dto;

import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.menu.Dish;

import java.math.BigDecimal;
import java.util.List;

/** Admin dish row (PRD §4.3): full dish info for the Console table and edit form. */
public record AdminDishView(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Category category,
        String protein,
        SpiceLevel spiceLevel,
        List<Allergen> allergens,
        String imageUrl,
        boolean active) {

    public static AdminDishView of(Dish dish) {
        return new AdminDishView(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getCategory(),
                dish.getProtein(),
                dish.getSpiceLevel(),
                List.copyOf(dish.getAllergens()),
                dish.getImageUrl(),
                dish.isActive());
    }
}
