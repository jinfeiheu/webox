package com.webox.order.dto;

import com.webox.common.enums.MealSlot;
import com.webox.common.enums.OrderStatus;
import com.webox.common.option.SelectedOption;
import com.webox.order.Order;
import com.webox.order.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Full order view (success page / order detail). All fields are snapshots. */
public record OrderView(
        Long orderId,
        String orderNo,
        LocalDate deliveryDate,
        MealSlot mealSlot,
        String address,
        BigDecimal totalPrice,
        OrderStatus status,
        Instant createdAt,
        List<OrderItemView> items) {

    public record OrderItemView(
            Long dishId,
            String dishName,
            BigDecimal unitPrice,
            List<SelectedOption> options,
            int qty,
            BigDecimal subtotal) {

        static OrderItemView of(OrderItem item) {
            return new OrderItemView(
                    item.getDishId(),
                    item.getDishName(),
                    item.getUnitPrice(),
                    item.getOptions(),
                    item.getQty(),
                    item.getSubtotal());
        }
    }

    public static OrderView of(Order order) {
        return new OrderView(
                order.getId(),
                order.getOrderNo(),
                order.getDeliveryDate(),
                order.getMealSlot(),
                order.getAddress(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemView::of).toList());
    }
}
