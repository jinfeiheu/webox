package com.webox.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money convention (PRD §6): CNY, exact to the cent.
 * DB stores DECIMAL(10,2); all computation uses BigDecimal with scale 2 — never float/double.
 */
public final class Moneys {

    public static final int SCALE = 2;

    private Moneys() {
    }

    /** Normalizes to scale 2, HALF_UP (e.g. new BigDecimal("22.5") -> 22.50). */
    public static BigDecimal of(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(String value) {
        return of(new BigDecimal(value));
    }

    /** unit price * quantity, result scaled to cents. */
    public static BigDecimal times(BigDecimal unitPrice, int quantity) {
        return of(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
