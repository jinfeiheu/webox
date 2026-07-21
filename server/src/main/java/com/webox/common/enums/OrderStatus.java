package com.webox.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Order status, English enum per PRD §3.4. Only PENDING orders can be cancelled. */
public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED;
    }

    @JsonCreator
    public static OrderStatus from(String value) {
        for (OrderStatus s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }
}
