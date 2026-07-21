package com.webox.cart.dto;

import com.webox.common.money.Moneys;

import java.math.BigDecimal;
import java.util.List;

public record CartView(List<CartItemView> items, int totalQty, BigDecimal totalPrice) {

    public static CartView of(List<CartItemView> items) {
        int totalQty = items.stream().mapToInt(CartItemView::qty).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItemView::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(items, totalQty, Moneys.of(totalPrice));
    }
}
