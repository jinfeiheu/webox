package com.webox.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddCartItemRequest(
        @NotNull(message = "dishId is required.")
        Long dishId,

        @Valid
        List<OptionSelection> selectedOptions,

        @Min(value = 1, message = "Quantity must be at least 1.")
        @Max(value = 5, message = "Quantity must be at most 5.")
        Integer qty,

        /** Set after the employee confirms the allergen warning dialog (PRD §4.1). */
        Boolean confirmed) {

    public record OptionSelection(@NotNull Long groupId, @NotNull Long itemId) {
    }
}
