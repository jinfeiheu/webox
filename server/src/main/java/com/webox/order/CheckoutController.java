package com.webox.order;

import com.webox.auth.AuthContext;
import com.webox.common.enums.MealSlot;
import com.webox.order.dto.CheckoutSummaryView;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/checkout/summary")
    public CheckoutSummaryView summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String slot) {
        MealSlot mealSlot = (slot == null || slot.isBlank()) ? null : MealSlot.from(slot);
        return orderService.getCheckoutSummary(AuthContext.require().id(), date, mealSlot);
    }

    /** Recent delivery addresses for the checkout picker (PRD §3.4). */
    @GetMapping("/addresses")
    public List<String> addresses() {
        return orderService.getAddressHistory(AuthContext.require().id());
    }
}
