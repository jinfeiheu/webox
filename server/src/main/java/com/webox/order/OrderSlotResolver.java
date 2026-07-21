package com.webox.order;

import com.webox.common.enums.MealSlot;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/**
 * Cutoff rules (PRD §4.2): lunch orders by 10:00, dinner orders by 15:00 same-day.
 * A requested slot past its cutoff does NOT fail — it auto-switches to the nearest
 * available slot: today's dinner if still open, otherwise tomorrow's lunch.
 */
@Component
public class OrderSlotResolver {

    public static final LocalTime LUNCH_CUTOFF = LocalTime.of(10, 0);
    public static final LocalTime DINNER_CUTOFF = LocalTime.of(15, 0);

    private final Clock clock;

    public OrderSlotResolver(Clock clock) {
        this.clock = clock;
    }

    public record SlotSelection(LocalDate date, MealSlot slot, boolean switched) {
    }

    public SlotSelection resolve(LocalDate requestedDate, MealSlot requestedSlot) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        if (requestedSlot == null) {
            return defaultSelection(today, now);
        }
        LocalDate date = requestedDate == null ? today : requestedDate;

        if (date.isAfter(today)) {
            // A future day: its cutoff only applies when the day comes.
            return new SlotSelection(date, requestedSlot, false);
        }
        if (date.isBefore(today)) {
            // A past day can never be ordered for; fall forward to the nearest open slot.
            return new SlotSelection(defaultSelection(today, now).date(),
                    defaultSelection(today, now).slot(), true);
        }
        // Same day: open if before the slot's cutoff.
        boolean open = requestedSlot == MealSlot.LUNCH
                ? now.isBefore(LUNCH_CUTOFF)
                : now.isBefore(DINNER_CUTOFF);
        if (open) {
            return new SlotSelection(today, requestedSlot, false);
        }
        // Auto-switch (§4.2): missed lunch but dinner is open -> today's dinner;
        // dinner also closed -> tomorrow's lunch.
        if (requestedSlot == MealSlot.LUNCH && now.isBefore(DINNER_CUTOFF)) {
            return new SlotSelection(today, MealSlot.DINNER, true);
        }
        return new SlotSelection(today.plusDays(1), MealSlot.LUNCH, true);
    }

    /** Default when the employee did not pick a slot: the nearest open one. */
    private SlotSelection defaultSelection(LocalDate today, LocalTime now) {
        if (now.isBefore(LUNCH_CUTOFF)) {
            return new SlotSelection(today, MealSlot.LUNCH, false);
        }
        if (now.isBefore(DINNER_CUTOFF)) {
            return new SlotSelection(today, MealSlot.DINNER, false);
        }
        return new SlotSelection(today.plusDays(1), MealSlot.LUNCH, false);
    }
}
