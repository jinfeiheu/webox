package com.webox.order;

import com.webox.auth.AuthContext;
import com.webox.order.dto.OrderView;
import com.webox.order.dto.PlaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
