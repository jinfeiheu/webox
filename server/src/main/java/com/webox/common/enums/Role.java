package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** User role. EMPLOYEE cannot access Console endpoints (PRD §4.3). */
public enum Role {
    EMPLOYEE("Employee"),
    ADMIN("Admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Role from(String value) {
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(value) || r.label.equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
