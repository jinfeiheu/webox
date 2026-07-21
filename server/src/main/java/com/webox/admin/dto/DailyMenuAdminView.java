package com.webox.admin.dto;

import com.webox.menu.DailyMenu;
import com.webox.menu.Dish;

import java.time.LocalDate;
import java.util.List;

/** Admin view of a day's menu (PRD §4.3): every active dish, flagged if already on the menu. */
public record DailyMenuAdminView(LocalDate date, List<Entry> entries) {

    public record Entry(Long dishId, String name, String imageUrl, String category,
                        boolean selected, int stockTotal) {

        public static Entry of(Dish dish, DailyMenu dailyMenu) {
            return new Entry(
                    dish.getId(),
                    dish.getName(),
                    dish.getImageUrl(),
                    dish.getCategory().getLabel(),
                    dailyMenu != null,
                    dailyMenu == null ? 0 : dailyMenu.getStockTotal());
        }
    }
}
