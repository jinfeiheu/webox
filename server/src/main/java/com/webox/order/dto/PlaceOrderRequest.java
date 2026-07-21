package com.webox.order.dto;

import com.webox.common.enums.MealSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlaceOrderRequest(
        /** Defaults to today when absent. */
        LocalDate deliveryDate,

        /** Defaults to the nearest open slot when absent (PRD §4.2 auto-switch). */
        MealSlot mealSlot,

        @NotBlank(message = "Delivery address is required.")
        @Size(max = 200, message = "Address must be at most 200 characters.")
        String address,

        /** Client-generated per checkout attempt; retries must reuse the same key (PRD §3.4). */
        @NotBlank(message = "idempotencyKey is required.")
        @Size(min = 8, max = 64)
        String idempotencyKey) {
}
