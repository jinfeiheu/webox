package com.webox.order;

import com.webox.auth.UserRepository;
import com.webox.cart.CartItem;
import com.webox.cart.CartItemRepository;
import com.webox.cart.CartService;
import com.webox.cart.dto.CartItemView;
import com.webox.cart.dto.CartView;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.MealSlot;
import com.webox.common.enums.OrderStatus;
import com.webox.common.money.Moneys;
import com.webox.common.option.SelectedOption;
import com.webox.inventory.InventoryEvent;
import com.webox.inventory.InventorySseService;
import com.webox.menu.DailyMenu;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.order.dto.CheckoutSummaryView;
import com.webox.order.dto.OrderView;
import com.webox.order.dto.PlaceOrderRequest;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Order placement — the system's choke point (PRD §3.4/§4.2), in strict order:
 * idempotent replay -> cutoff slot resolution -> cart checks (empty / 5-item cap) ->
 * one-active-order-per-slot -> snapshot persist -> cart clear.
 * Stock deduction is layered on in T15 without changing this flow's shape.
 */
@Service
public class OrderService {

    private static final List<OrderStatus> ACTIVE_STATUSES =
            List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderSlotResolver slotResolver;
    private final DailyMenuRepository dailyMenuRepository;
    private final CacheManager cacheManager;
    private final InventorySseService sseService;
    private final TransactionTemplate newTxTemplate;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        CartService cartService, UserRepository userRepository,
                        OrderSlotResolver slotResolver, DailyMenuRepository dailyMenuRepository,
                        CacheManager cacheManager, InventorySseService sseService,
                        PlatformTransactionManager txManager) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.slotResolver = slotResolver;
        this.dailyMenuRepository = dailyMenuRepository;
        this.cacheManager = cacheManager;
        this.sseService = sseService;
        // For the race-recovery reads below: they must run OUTSIDE the (doomed) current tx.
        this.newTxTemplate = new TransactionTemplate(txManager);
        this.newTxTemplate.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public OrderView placeOrder(Long userId, PlaceOrderRequest request) {
        // 1. Idempotent replay: this key already created an order -> return it, create nothing.
        var byKey = orderRepository.findByIdempotencyKey(request.idempotencyKey());
        if (byKey.isPresent()) {
            Order existing = byKey.get();
            if (!existing.getUser().getId().equals(userId)) {
                throw new BizException(ErrorCode.VALIDATION_ERROR, "Invalid idempotency key.");
            }
            return OrderView.of(reloadWithItems(existing.getId()));
        }

        // 2. Cutoff rules: resolve (and possibly auto-switch) the delivery slot.
        OrderSlotResolver.SlotSelection slot =
                slotResolver.resolve(request.deliveryDate(), request.mealSlot());

        // 3. Cart-driven checks.
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithDish(userId);
        if (cartItems.isEmpty()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "Your cart is empty.");
        }
        int totalQty = cartItems.stream().mapToInt(CartItem::getQty).sum();
        if (totalQty > CartService.MAX_TOTAL_QTY) {
            throw new BizException(ErrorCode.ORDER_LIMIT_EXCEEDED);
        }

        // 4. One active order per (user, date, slot).
        orderRepository.findActiveBySlot(userId, slot.date(), slot.slot(), ACTIVE_STATUSES)
                .ifPresent(existing -> {
                    throw orderExists(existing);
                });

        // 5. Stock deduction — atomic per-dish decrement; any shortfall aborts the whole
        //    order within the same transaction (PRD §5.1: no oversell).
        Map<Long, Integer> qtyByDish = new LinkedHashMap<>();
        for (CartItem ci : cartItems) {
            qtyByDish.merge(ci.getDish().getId(), ci.getQty(), Integer::sum);
        }
        List<String> outOfStock = new ArrayList<>();
        LocalDate orderDate = slot.date();
        for (var entry : qtyByDish.entrySet()) {
            int affected = dailyMenuRepository.decrementStock(
                    entry.getKey(), orderDate, entry.getValue());
            if (affected == 0) {
                DailyMenu dm = dailyMenuRepository
                        .findByMenuDateAndDishId(orderDate, entry.getKey()).orElse(null);
                String name = dm != null ? dm.getDish().getName() : "Dish #" + entry.getKey();
                int left = dm != null ? dm.getStockRemaining() : 0;
                outOfStock.add(name + " (only " + left + " left)");
            }
        }
        if (!outOfStock.isEmpty()) {
            throw new BizException(ErrorCode.STOCK_INSUFFICIENT,
                    ErrorCode.STOCK_INSUFFICIENT.getDefaultMessage(), outOfStock);
        }

        // 6. Build the order with full line snapshots (current dish price + option extras).
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(userRepository.getReferenceById(userId));
        order.setDeliveryDate(slot.date());
        order.setMealSlot(slot.slot());
        order.setAddress(request.address().trim());
        order.setIdempotencyKey(request.idempotencyKey());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Dish dish = cartItem.getDish();
            BigDecimal extras = cartItem.getSelectedOptions().stream()
                    .map(SelectedOption::extraPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal unitPrice = Moneys.of(dish.getPrice().add(extras));
            BigDecimal subtotal = Moneys.times(unitPrice, cartItem.getQty());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setOptions(cartItem.getSelectedOptions());
            orderItem.setQty(cartItem.getQty());
            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }
        order.setTotal(Moneys.of(total));

        // 6. Persist. The two unique indexes (idempotency_key, active_key) are the DB-level
        //    backstop: if a concurrent twin request won the race, recover by replaying.
        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException race) {
            return recoverFromRace(userId, request, slot);
        }

        // 7. Clear the cart only after the order is durably saved.
        cartItemRepository.deleteByUserId(userId);
        // Stock changed — flush the stale menu snapshot in cache for this day.
        Objects.requireNonNull(cacheManager.getCache("menuItems")).evict(slot.date());
        // Push real-time inventory updates to every connected browser (PRD §5.1).
        for (var entry : qtyByDish.entrySet()) {
            DailyMenu dm = dailyMenuRepository
                    .findByMenuDateAndDishId(orderDate, entry.getKey()).orElse(null);
            if (dm != null) {
                sseService.publish(new InventoryEvent(
                        entry.getKey(), orderDate, dm.getStockRemaining()));
            }
        }
        return OrderView.of(order);
    }

    @Transactional(readOnly = true)
    public CheckoutSummaryView getCheckoutSummary(Long userId, LocalDate date, MealSlot slot) {
        OrderSlotResolver.SlotSelection selection = slotResolver.resolve(date, slot);
        CartView cart = cartService.view(userId);
        List<CartItemView> items = cart.items();
        CheckoutSummaryView.ExistingOrderRef existing =
                orderRepository.findActiveBySlot(userId, selection.date(), selection.slot(), ACTIVE_STATUSES)
                        .map(o -> new CheckoutSummaryView.ExistingOrderRef(o.getId(), o.getOrderNo()))
                        .orElse(null);
        return new CheckoutSummaryView(
                selection.date(), selection.slot(), selection.switched(),
                items, cart.totalQty(), cart.totalPrice(), existing);
    }

    @Transactional(readOnly = true)
    public List<String> getAddressHistory(Long userId) {
        return orderRepository.findAddressHistory(userId, PageRequest.of(0, 5));
    }

    @Transactional(readOnly = true)
    public List<com.webox.order.dto.OrderSummaryView> listOrders(Long userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(com.webox.order.dto.OrderSummaryView::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderView getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Order not found."));
        return OrderView.of(order);
    }

    /** Cancel (PRD §3.4) + T17 stock restore: restore stock FIRST so the @Modifying
     *  clearAutomatically does not erase the order's status change. */
    @Transactional
    public OrderView cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Order not found."));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BizException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }

        // Restore stock for every line item BEFORE changing the order status,
        // because the @Modifying(clearAutomatically) on incrementStock would
        // otherwise clear the persistence context and discard the save() below.
        LocalDate orderDate = order.getDeliveryDate();
        for (OrderItem item : order.getItems()) {
            dailyMenuRepository.incrementStock(item.getDishId(), orderDate, item.getQty());
            DailyMenu dm = dailyMenuRepository
                    .findByMenuDateAndDishId(orderDate, item.getDishId()).orElse(null);
            if (dm != null) {
                sseService.publish(new InventoryEvent(
                        item.getDishId(), orderDate, dm.getStockRemaining()));
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        Objects.requireNonNull(cacheManager.getCache("menuItems")).evict(orderDate);
        return OrderView.of(order);
    }

    /** A unique-violation means a concurrent twin already committed — replay, don't fail. */
    private OrderView recoverFromRace(Long userId, PlaceOrderRequest request,
                                      OrderSlotResolver.SlotSelection slot) {
        return newTxTemplate.execute(status -> {
            var byKey = orderRepository.findByIdempotencyKey(request.idempotencyKey());
            if (byKey.isPresent()) {
                return OrderView.of(reloadWithItems(byKey.get().getId()));
            }
            var active = orderRepository.findActiveBySlot(userId, slot.date(), slot.slot(), ACTIVE_STATUSES);
            if (active.isPresent()) {
                throw orderExists(active.get());
            }
            // Neither unique key fired — rethrow the original problem.
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to place the order. Please try again.");
        });
    }

    private static BizException orderExists(Order existing) {
        // details[0] carries the existing order id so the UI can deep-link to it.
        return new BizException(ErrorCode.ORDER_EXISTS,
                ErrorCode.ORDER_EXISTS.getDefaultMessage(),
                List.of(String.valueOf(existing.getId())));
    }

    private Order reloadWithItems(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Order not found."));
    }

    private static String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "WB" + date + "-" + suffix;
    }
}
