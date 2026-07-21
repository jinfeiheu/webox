package com.webox.menu.dto;

import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.menu.Dish;

import java.math.BigDecimal;
import java.util.List;

/** Full dish detail including customization option groups (PRD §3.2). */
public record DishDetailView(
        Long dishId,
        String name,
        String description,
        BigDecimal price,
        Category category,
        String protein,
        SpiceLevel spiceLevel,
        List<Allergen> allergens,
        String imageUrl,
        List<OptionGroupView> optionGroups) {

    public record OptionGroupView(Long id, String name, boolean required, List<OptionItemView> items) {
    }

    public record OptionItemView(Long id, String name, BigDecimal extraPrice) {
    }

    public static DishDetailView of(Dish dish) {
        List<OptionGroupView> groups = dish.getOptionGroups().stream()
                .map(g -> new OptionGroupView(
                        g.getId(),
                        g.getName(),
                        g.isRequired(),
                        g.getItems().stream()
                                .map(i -> new OptionItemView(i.getId(), i.getName(), i.getExtraPrice()))
                                .toList()))
                .toList();
        return new DishDetailView(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getCategory(),
                dish.getProtein(),
                dish.getSpiceLevel(),
                List.copyOf(dish.getAllergens()),
                dish.getImageUrl(),
                groups);
    }
}
