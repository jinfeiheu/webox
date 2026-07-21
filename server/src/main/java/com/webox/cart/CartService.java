package com.webox.cart;

import com.webox.auth.UserRepository;
import com.webox.cart.dto.AddCartItemRequest;
import com.webox.cart.dto.AddCartItemResponse;
import com.webox.cart.dto.CartItemView;
import com.webox.cart.dto.CartView;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import com.webox.menu.OptionGroup;
import com.webox.menu.OptionItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Service
public class CartService {

    /** Per-order total-quantity cap (PRD §4.2) — enforced on the cart, which feeds one order. */
    public static final int MAX_TOTAL_QTY = 5;

    private final CartItemRepository cartItemRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, DishRepository dishRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartView view(Long userId) {
        List<CartItemView> items = cartItemRepository.findByUserIdWithDish(userId).stream()
                .map(CartItemView::of)
                .toList();
        return CartView.of(items);
    }

    @Transactional
    public AddCartItemResponse add(Long userId, AddCartItemRequest request) {
        int qty = request.qty() == null ? 1 : request.qty();
        enforceLimit(userId, qty, null);

        Dish dish = dishRepository.findById(request.dishId())
                .filter(Dish::isActive)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Dish not found or unavailable."));

        List<SelectedOption> snapshot = resolveOptions(dish, request.selectedOptions());
        String optionsHash = optionsHash(request.dishId(), snapshot);

        CartItem item = cartItemRepository
                .findByUserIdAndDishIdAndOptionsHash(userId, dish.getId(), optionsHash)
                .orElse(null);
        if (item != null) {
            // same dish, same configuration -> merge quantities (PRD §3.3)
            item.setQty(item.getQty() + qty);
        } else {
            item = new CartItem();
            item.setUser(userRepository.getReferenceById(userId));
            item.setDish(dish);
            item.setQty(qty);
            item.setSelectedOptions(snapshot);
            item.setOptionsHash(optionsHash);
        }
        cartItemRepository.save(item);

        // Allergen matching against the employee's flags is wired in T11 (preferences);
        // with no preferences configured the match set is always empty (PRD §4.1: warn, never filter).
        return new AddCartItemResponse(CartItemView.of(item), List.of());
    }

    @Transactional
    public CartItemView updateQty(Long userId, Long cartItemId, int qty) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Cart item not found."));
        enforceLimit(userId, qty, item);
        item.setQty(qty);
        cartItemRepository.save(item);
        return CartItemView.of(item);
    }

    @Transactional
    public void remove(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Cart item not found."));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    /** Validates selections against the dish's groups and returns an immutable snapshot. */
    private List<SelectedOption> resolveOptions(Dish dish, List<AddCartItemRequest.OptionSelection> selections) {
        List<AddCartItemRequest.OptionSelection> requested =
                selections == null ? List.of() : selections;

        List<SelectedOption> snapshot = new ArrayList<>();
        for (AddCartItemRequest.OptionSelection selection : requested) {
            OptionGroup group = dish.getOptionGroups().stream()
                    .filter(g -> g.getId().equals(selection.groupId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.VALIDATION_ERROR,
                            "Invalid option selection for this dish."));
            OptionItem optionItem = group.getItems().stream()
                    .filter(i -> i.getId().equals(selection.itemId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.VALIDATION_ERROR,
                            "Invalid option selection for this dish."));
            boolean duplicate = snapshot.stream()
                    .anyMatch(s -> s.groupId().equals(group.getId()) && s.itemId().equals(optionItem.getId()));
            if (duplicate) {
                throw new BizException(ErrorCode.VALIDATION_ERROR, "Duplicate option selection.");
            }
            snapshot.add(new SelectedOption(
                    group.getId(), group.getName(), optionItem.getId(), optionItem.getName(),
                    optionItem.getExtraPrice()));
        }

        for (OptionGroup group : dish.getOptionGroups()) {
            if (!group.isRequired()) {
                continue;
            }
            long chosen = snapshot.stream().filter(s -> s.groupId().equals(group.getId())).count();
            if (chosen != 1) {
                throw new BizException(ErrorCode.VALIDATION_ERROR,
                        "Please choose exactly one option for \"" + group.getName() + "\".");
            }
        }
        return snapshot;
    }

    /**
     * Total cart quantity may never exceed {@link #MAX_TOTAL_QTY}.
     * When updating an existing line, pass it as `replacing` so its current qty is subtracted first.
     */
    private void enforceLimit(Long userId, int additionalQty, CartItem replacing) {
        int current = cartItemRepository.totalQtyByUserId(userId);
        int base = replacing == null ? current : current - replacing.getQty();
        if (base + additionalQty > MAX_TOTAL_QTY) {
            throw new BizException(ErrorCode.ORDER_LIMIT_EXCEEDED);
        }
    }

    /** Canonical, order-independent hash of (dish, option selection) for line dedup. */
    private static String optionsHash(Long dishId, List<SelectedOption> snapshot) {
        String canonical = dishId + "|" + snapshot.stream()
                .map(s -> s.groupId() + ":" + s.itemId())
                .sorted(Comparator.naturalOrder())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
