package com.webox.preference.dto;

import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.common.enums.TasteLevel;
import com.webox.preference.UserPreference;

import java.math.BigDecimal;
import java.util.List;

/** Preference payload — used for both GET (view) and PUT (update) of /api/preferences. */
public record PreferenceView(
        List<Allergen> allergens,
        List<Category> cuisines,
        SpiceLevel spiceLevel,
        TasteLevel taste,
        BigDecimal budgetMin,
        BigDecimal budgetMax) {

    public static PreferenceView empty() {
        return new PreferenceView(List.of(), List.of(), null, null, null, null);
    }

    public static PreferenceView of(UserPreference preference) {
        return new PreferenceView(
                preference.getAllergens(),
                preference.getCuisines(),
                preference.getSpiceLevel(),
                preference.getTaste(),
                preference.getBudgetMin(),
                preference.getBudgetMax());
    }
}
