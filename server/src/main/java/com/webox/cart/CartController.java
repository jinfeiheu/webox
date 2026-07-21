package com.webox.cart;

import com.webox.auth.AuthContext;
import com.webox.auth.AuthUser;
import com.webox.cart.dto.AddCartItemRequest;
import com.webox.cart.dto.AddCartItemResponse;
import com.webox.cart.dto.CartItemView;
import com.webox.cart.dto.CartView;
import com.webox.cart.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartView view() {
        return cartService.view(currentUserId());
    }

    @PostMapping("/items")
    public AddCartItemResponse add(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.add(currentUserId(), request);
    }

    @PatchMapping("/items/{id}")
    public CartItemView updateQty(@PathVariable("id") long cartItemId,
                                  @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQty(currentUserId(), cartItemId, request.qty());
    }

    @DeleteMapping("/items/{id}")
    public void remove(@PathVariable("id") long cartItemId) {
        cartService.remove(currentUserId(), cartItemId);
    }

    private static Long currentUserId() {
        AuthUser user = AuthContext.require();
        return user.id();
    }
}
