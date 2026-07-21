package com.webox.order;

import com.webox.auth.AuthContext;
import com.webox.order.dto.OrderSummaryView;
import com.webox.order.dto.OrderView;
import com.webox.order.dto.PlaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderView placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(AuthContext.require().id(), request);
    }

    @GetMapping
    public List<OrderSummaryView> list() {
        return orderService.listOrders(AuthContext.require().id());
    }

    @GetMapping("/{id}")
    public OrderView detail(@PathVariable("id") long orderId) {
        return orderService.getOrder(AuthContext.require().id(), orderId);
    }

    @PostMapping("/{id}/cancel")
    public OrderView cancel(@PathVariable("id") long orderId) {
        return orderService.cancelOrder(AuthContext.require().id(), orderId);
    }
}
