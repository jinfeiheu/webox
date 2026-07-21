package com.webox.cart.dto;

import com.webox.cart.CartItem;
import com.webox.common.option.SelectedOption;
import com.webox.common.enums.Category;
import com.webox.common.money.Moneys;
import com.webox.menu.Dish;

import java.math.BigDecimal;
import java.util.List;

/** One cart line as shown in the UI; prices recomputed live from the current dish price. */
public record CartItemView(
        Long cartItemId,
        Long dishId,
        String dishName,
        String imageUrl,
        Category category,
        BigDecimal unitPrice,
        List<SelectedOption> selectedOptions,
        int qty,
        BigDecimal subtotal) {

    public static CartItemView of(CartItem item) {
        Dish dish = item.getDish();
        BigDecimal extras = item.getSelectedOptions().stream()
                .map(SelectedOption::extraPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unitPrice = Moneys.of(dish.getPrice().add(extras));
        return new CartItemView(
                item.getId(),
                dish.getId(),
                dish.getName(),
                dish.getImageUrl(),
                dish.getCategory(),
                unitPrice,
                item.getSelectedOptions(),
                item.getQty(),
                Moneys.times(unitPrice, item.getQty()));
    }
}
