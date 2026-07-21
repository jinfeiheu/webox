package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Cuisine category, fixed enum per PRD §6. Display labels are shown verbatim in UI. */
public enum Category {
    CHINESE("Chinese"),
    WESTERN("Western"),
    JAPANESE("Japanese"),
    LIGHT_MEAL("Light Meal"),
    KOREAN("Korean"),
    SOUTHEAST_ASIAN("Southeast Asian");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Category from(String value) {
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(value) || c.label.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }
}
