package com.webox.order;

import com.webox.common.enums.MealSlot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cutoff auto-switch rules (PRD §4.2): lunch by 10:00, dinner by 15:00 same-day;
 * a past-cutoff slot auto-switches to today's dinner (if open) else tomorrow's lunch.
 * Pure unit test with a fixed Clock — no database.
 */
class OrderSlotResolverTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

    private OrderSlotResolver at(int hour, int minute) {
        Instant instant = TODAY.atTime(hour, minute).atZone(ZONE).toInstant();
        return new OrderSlotResolver(Clock.fixed(instant, ZONE));
    }

    @Test
    void beforeLunchCutoff_keepsRequestedLunch() {
        var sel = at(9, 0).resolve(TODAY, MealSlot.LUNCH);
        assertThat(sel.date()).isEqualTo(TODAY);
        assertThat(sel.slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(sel.switched()).isFalse();
    }

    @Test
    void beforeLunchCutoff_dinnerStillOpen() {
        var sel = at(9, 0).resolve(TODAY, MealSlot.DINNER);
        assertThat(sel.date()).isEqualTo(TODAY);
        assertThat(sel.slot()).isEqualTo(MealSlot.DINNER);
        assertThat(sel.switched()).isFalse();
    }

    @Test
    void noSlot_defaultsToNearestOpen() {
        assertThat(at(9, 0).resolve(null, null).slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(at(10, 30).resolve(null, null).slot()).isEqualTo(MealSlot.DINNER);
        assertThat(at(15, 30).resolve(null, null).slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(at(15, 30).resolve(null, null).date()).isEqualTo(TODAY.plusDays(1));
    }

    @Test
    void lunchPastCutoff_dinnerStillOpen_switchesToTodayDinner() {
        var sel = at(10, 30).resolve(TODAY, MealSlot.LUNCH);
        assertThat(sel.date()).isEqualTo(TODAY);
        assertThat(sel.slot()).isEqualTo(MealSlot.DINNER);
        assertThat(sel.switched()).isTrue();
    }

    @Test
    void lunchPastCutoff_dinnerAlsoClosed_switchesToTomorrowLunch() {
        var sel = at(15, 30).resolve(TODAY, MealSlot.LUNCH);
        assertThat(sel.date()).isEqualTo(TODAY.plusDays(1));
        assertThat(sel.slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(sel.switched()).isTrue();
    }

    @Test
    void dinnerPastCutoff_switchesToTomorrowLunch() {
        var sel = at(15, 30).resolve(TODAY, MealSlot.DINNER);
        assertThat(sel.date()).isEqualTo(TODAY.plusDays(1));
        assertThat(sel.slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(sel.switched()).isTrue();
    }

    @Test
    void dinnerBeforeItsCutoff_keepsRequestedDinner() {
        var sel = at(14, 0).resolve(TODAY, MealSlot.DINNER);
        assertThat(sel.date()).isEqualTo(TODAY);
        assertThat(sel.slot()).isEqualTo(MealSlot.DINNER);
        assertThat(sel.switched()).isFalse();
    }

    @Test
    void futureDate_acceptedAsRequested() {
        var sel = at(9, 0).resolve(TODAY.plusDays(3), MealSlot.LUNCH);
        assertThat(sel.date()).isEqualTo(TODAY.plusDays(3));
        assertThat(sel.slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(sel.switched()).isFalse();
    }

    @Test
    void pastDate_fallsForwardToNearestOpen() {
        var sel = at(9, 0).resolve(TODAY.minusDays(1), MealSlot.LUNCH);
        assertThat(sel.date()).isEqualTo(TODAY);
        assertThat(sel.slot()).isEqualTo(MealSlot.LUNCH);
        assertThat(sel.switched()).isTrue();
    }
}
