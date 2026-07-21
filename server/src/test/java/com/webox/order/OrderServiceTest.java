package com.webox.order;

import com.webox.auth.User;
import com.webox.cart.CartItem;
import com.webox.cart.CartService;
import com.webox.cart.CartItemRepository;
import com.webox.auth.UserRepository;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.MealSlot;
import com.webox.common.enums.OrderStatus;
import com.webox.common.money.Moneys;
import com.webox.inventory.InventorySseService;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.order.dto.OrderView;
import com.webox.order.dto.PlaceOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Order placement decision logic (PRD §3.4/§4.2) — Mockito unit test, no database.
 * Covers: idempotent replay, empty cart, 5-item cap, one-active-order-per-slot, cancel rules.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

    @Mock OrderRepository orderRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock CartService cartService;
    @Mock UserRepository userRepository;
    @Mock OrderSlotResolver slotResolver;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock CacheManager cacheManager;
    @Mock InventorySseService sseService;
    @Mock PlatformTransactionManager txManager;

    @InjectMocks OrderService service;

    private Dish dish;
    private CartItem cartItem;

    @Mock Cache menuItemsCache;

    @BeforeEach
    void setup() {
        dish = new Dish();
        dish.setName("Kung Pao Chicken");
        dish.setPrice(Moneys.of("22.00"));
        dish.setAllergens(List.of());

        cartItem = new CartItem();
        cartItem.setDish(dish);
        cartItem.setQty(1);
        cartItem.setSelectedOptions(List.of());
    }

    /** Only tests that actually place a successful order need stock/cache mocks wired. */
    private void stockAndCacheSucceed() {
        when(dailyMenuRepository.decrementStock(any(), any(), anyInt())).thenReturn(1);
        when(cacheManager.getCache("menuItems")).thenReturn(menuItemsCache);
    }

    private void slotResolvesToTodayLunch() {
        when(slotResolver.resolve(null, null))
                .thenReturn(new OrderSlotResolver.SlotSelection(TODAY, MealSlot.LUNCH, false));
    }

    private PlaceOrderRequest request(String key) {
        return new PlaceOrderRequest(null, null, "Building A", key);
    }

    @Test
    void placeOrder_happyPath_createsOrderAndClearsCart() {
        slotResolvesToTodayLunch();
        stockAndCacheSucceed();
        when(orderRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdWithDish(USER_ID)).thenReturn(List.of(cartItem));
        when(orderRepository.findActiveBySlot(eq(USER_ID), eq(TODAY), eq(MealSlot.LUNCH), any()))
                .thenReturn(Optional.empty());
        when(orderRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderView result = service.placeOrder(USER_ID, request("k1"));

        assertThat(result.orderNo()).startsWith("WB");
        assertThat(result.mealSlot()).isEqualTo(MealSlot.LUNCH);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalPrice()).isEqualByComparingTo("22.00");
        verify(cartItemRepository).deleteByUserId(USER_ID);
    }

    @Test
    void placeOrder_sameIdempotencyKey_returnsExistingOrderWithoutSaving() {
        Order existing = buildExistingOrder("WB20260721-ABC", OrderStatus.PENDING);
        when(orderRepository.findByIdempotencyKey("k1")).thenReturn(Optional.of(existing));
        when(orderRepository.findWithItemsById(existing.getId())).thenReturn(Optional.of(existing));

        OrderView result = service.placeOrder(USER_ID, request("k1"));

        assertThat(result.orderNo()).isEqualTo("WB20260721-ABC");
        verify(orderRepository, never()).saveAndFlush(any());
        verify(cartItemRepository, never()).deleteByUserId(USER_ID);
    }

    @Test
    void placeOrder_emptyCart_rejected() {
        slotResolvesToTodayLunch();
        when(orderRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdWithDish(USER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> service.placeOrder(USER_ID, request("k1")))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void placeOrder_overFiveItems_rejected() {
        CartItem heavy = new CartItem();
        heavy.setDish(dish);
        heavy.setQty(6);
        slotResolvesToTodayLunch();
        when(orderRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdWithDish(USER_ID)).thenReturn(List.of(heavy));

        assertThatThrownBy(() -> service.placeOrder(USER_ID, request("k1")))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_LIMIT_EXCEEDED);
    }

    @Test
    void placeOrder_existingActiveOrder_rejectedWithExistingId() {
        Order active = buildExistingOrder("WB20260721-OLD", OrderStatus.PENDING);
        slotResolvesToTodayLunch();
        when(orderRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdWithDish(USER_ID)).thenReturn(List.of(cartItem));
        when(orderRepository.findActiveBySlot(eq(USER_ID), eq(TODAY), eq(MealSlot.LUNCH), any()))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.placeOrder(USER_ID, request("k1")))
                .isInstanceOf(BizException.class)
                .satisfies(e -> {
                    BizException b = (BizException) e;
                    assertThat(b.getErrorCode()).isEqualTo(ErrorCode.ORDER_EXISTS);
                    assertThat(b.getDetails()).contains(String.valueOf(active.getId()));
                });
    }

    @Test
    void cancelOrder_pendingOrder_becomesCancelled() {
        Order order = buildExistingOrder("WB20260721-X", OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserId(order.getId(), USER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // T17: cancel restores stock + emits SSE + evicts cache.
        when(cacheManager.getCache("menuItems")).thenReturn(menuItemsCache);

        OrderView result = service.cancelOrder(USER_ID, order.getId());

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_nonPendingOrder_rejected() {
        Order order = buildExistingOrder("WB20260721-Y", OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndUserId(order.getId(), USER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(USER_ID, order.getId()))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_NOT_CANCELLABLE);
    }

    private Order buildExistingOrder(String orderNo, OrderStatus status) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(status);
        order.setDeliveryDate(TODAY);
        order.setMealSlot(MealSlot.LUNCH);
        order.setAddress("Building A");
        order.setIdempotencyKey("seed-" + orderNo);
        order.setTotal(Moneys.of("22.00"));
        User user = new User();
        // User.id has no setter; reflectively set for the test's ownership check.
        setField(user, "id", USER_ID);
        order.setUser(user);
        order.getItems().add(new com.webox.order.OrderItem());
        return order;
    }

    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
