package com.webox.admin;

import com.webox.admin.dto.DashboardView;
import com.webox.common.enums.MealSlot;
import com.webox.common.money.Moneys;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import com.webox.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Aggregates business metrics from real orders (PRD §5.3). */
@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final DailyMenuRepository dailyMenuRepository;

    public DashboardService(OrderRepository orderRepository, DailyMenuRepository dailyMenuRepository) {
        this.orderRepository = orderRepository;
        this.dailyMenuRepository = dailyMenuRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboard() {
        LocalDate today = LocalDate.now();

        // Today overview.
        var todayCounts = orderRepository.countAndRevenueByDeliveryDate(today);
        long totalOrders = todayCounts.stream().mapToLong(r -> ((Number) r[0]).longValue()).sum();
        BigDecimal totalRevenue = todayCounts.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pending = orderRepository.countByDeliveryDateAndStatus(today, com.webox.common.enums.OrderStatus.PENDING);
        long confirmed = orderRepository.countByDeliveryDateAndStatus(today, com.webox.common.enums.OrderStatus.CONFIRMED);
        long completed = orderRepository.countByDeliveryDateAndStatus(today, com.webox.common.enums.OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByDeliveryDateAndStatus(today, com.webox.common.enums.OrderStatus.CANCELLED);

        // Top 10 dishes by total quantity sold (all time, non-cancelled).
        var topRaw = orderRepository.findTopDishes();
        List<DashboardView.TopDish> topDishes = topRaw.stream()
                .map(row -> new DashboardView.TopDish(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue()))
                .toList();

        // Lunch vs dinner today.
        long lunchCount = orderRepository.countByDeliveryDateAndMealSlot(
                today, MealSlot.LUNCH);
        long dinnerCount = orderRepository.countByDeliveryDateAndMealSlot(
                today, MealSlot.DINNER);

        // 7-day trend (non-cancelled).
        List<Object[]> trendRaw = orderRepository.trendBetween(
                today.minusDays(7), today);
        List<DashboardView.TrendPoint> trend = trendRaw.stream()
                .map(row -> new DashboardView.TrendPoint(
                        ((LocalDate) row[0]),
                        ((Number) row[1]).intValue(),
                        Moneys.of((BigDecimal) row[2])))
                .toList();

        // Low-stock items (≤3, on today's menu).
        List<DashboardView.LowStockItem> lowStock = dailyMenuRepository
                .findByMenuDate(today).stream()
                .filter(dm -> dm.getStockRemaining() > 0 && dm.getStockRemaining() <= 3)
                .map(dm -> new DashboardView.LowStockItem(
                        dm.getDish().getId(), dm.getDish().getName(), dm.getStockRemaining()))
                .toList();

        return new DashboardView(
                new DashboardView.TodayOverview(totalOrders, totalRevenue, pending, confirmed,
                        completed, cancelled),
                topDishes,
                new DashboardView.SlotDistribution(lunchCount, dinnerCount),
                trend,
                lowStock);
    }
}
