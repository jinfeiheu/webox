package com.webox.inventory;

import java.time.LocalDate;

/** SSE payload pushed to every connected client when stock changes (PRD §5.1). */
public record InventoryEvent(Long dishId, LocalDate menuDate, int stockRemaining) {
}
