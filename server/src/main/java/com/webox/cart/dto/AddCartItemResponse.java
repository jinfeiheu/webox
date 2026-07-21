package com.webox.cart.dto;

import com.webox.common.enums.Allergen;

import java.util.List;

/** POST /cart/items response: the affected line plus allergens matching the employee's flags (PRD §4.1). */
public record AddCartItemResponse(CartItemView item, List<Allergen> matchedAllergens) {
}
