package com.webox.order.dto;

import com.webox.cart.dto.CartItemView;
import com.webox.common.enums.MealSlot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Checkout page data (PRD §3.4/§4.2): the EFFECTIVE delivery slot after cutoff auto-switch
 * (`switched` flags that a switch happened), cart contents, and any existing active order
 * for that slot — the UI then shows "View Existing Order" instead of "Place Order".
 */
public record CheckoutSummaryView(
        LocalDate date,
        MealSlot slot,
        boolean switched,
        List<CartItemView> items,
        int totalQty,
        BigDecimal totalPrice,
        ExistingOrderRef existingOrder) {

    public record ExistingOrderRef(Long orderId, String orderNo) {
    }
}
