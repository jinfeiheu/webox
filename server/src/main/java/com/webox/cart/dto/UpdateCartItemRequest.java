package com.webox.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1.")
        @Max(value = 5, message = "Quantity must be at most 5.")
        Integer qty) {
}
