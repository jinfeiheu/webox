package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Taste richness preference (PRD §4.1): Light / Medium / Heavy — AI reference, no hard sort. */
public enum TasteLevel {
    LIGHT("Light"),
    MEDIUM("Medium"),
    HEAVY("Heavy");

    private final String label;

    TasteLevel(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static TasteLevel from(String value) {
        for (TasteLevel t : values()) {
            if (t.name().equalsIgnoreCase(value) || t.label.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown taste level: " + value);
    }
}
