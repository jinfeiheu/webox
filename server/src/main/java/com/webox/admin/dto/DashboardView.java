package com.webox.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Business dashboard payload (PRD §5.3). */
public record DashboardView(
        TodayOverview today,
        List<TopDish> topDishes,
        SlotDistribution slots,
        List<TrendPoint> trend,
        List<LowStockItem> lowStock) {

    public record TodayOverview(long totalOrders, BigDecimal totalRevenue,
                                long pending, long confirmed, long completed, long cancelled) {
    }

    public record TopDish(Long dishId, String dishName, int totalSold) {
    }

    public record SlotDistribution(long lunchCount, long dinnerCount) {
    }

    public record TrendPoint(LocalDate date, int orderCount, BigDecimal revenue) {
    }

    public record LowStockItem(Long dishId, String dishName, int stockRemaining) {
    }
}
