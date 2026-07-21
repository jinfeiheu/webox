package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Spice level, fixed enum per PRD §6: None / Mild / Medium / Hot. */
public enum SpiceLevel {
    NONE("None"),
    MILD("Mild"),
    MEDIUM("Medium"),
    HOT("Hot");

    private final String label;

    SpiceLevel(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static SpiceLevel from(String value) {
        for (SpiceLevel s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown spice level: " + value);
    }
}
