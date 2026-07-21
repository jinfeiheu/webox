package com.webox.order.dto;

import com.webox.common.enums.MealSlot;
import com.webox.common.enums.OrderStatus;
import com.webox.order.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** List-row view for "My Orders" (no line items). */
public record OrderSummaryView(
        Long orderId,
        String orderNo,
        LocalDate deliveryDate,
        MealSlot mealSlot,
        BigDecimal totalPrice,
        OrderStatus status,
        Instant createdAt,
        int itemCount) {

    public static OrderSummaryView of(Order order) {
        return new OrderSummaryView(
                order.getId(),
                order.getOrderNo(),
                order.getDeliveryDate(),
                order.getMealSlot(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().size());
    }
}
