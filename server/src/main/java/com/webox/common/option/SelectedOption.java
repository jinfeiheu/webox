package com.webox.common.option;

import java.math.BigDecimal;

/** Immutable snapshot of one chosen option, stored with the cart line (and later the order line). */
public record SelectedOption(Long groupId, String groupName, Long itemId, String itemName,
                             BigDecimal extraPrice) {
}
