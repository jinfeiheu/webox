package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Allergen, fixed full set per PRD §6. Seafood is always Fish / Shellfish —
 * never a separate "Seafood" value, so preference flags always match dish tags.
 */
public enum Allergen {
    PEANUTS("Peanuts"),
    DAIRY("Dairy"),
    EGG("Egg"),
    GLUTEN("Gluten"),
    SOY("Soy"),
    FISH("Fish"),
    SHELLFISH("Shellfish");

    private final String label;

    Allergen(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Allergen from(String value) {
        for (Allergen a : values()) {
            if (a.name().equalsIgnoreCase(value) || a.label.equalsIgnoreCase(value)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown allergen: " + value);
    }
}
