package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Meal slot. Cutoff: LUNCH 10:00, DINNER 15:00 same-day (PRD §4.2). */
public enum MealSlot {
    LUNCH("Lunch"),
    DINNER("Dinner");

    private final String label;

    MealSlot(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static MealSlot from(String value) {
        for (MealSlot m : values()) {
            if (m.name().equalsIgnoreCase(value) || m.label.equalsIgnoreCase(value)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Unknown meal slot: " + value);
    }
}
