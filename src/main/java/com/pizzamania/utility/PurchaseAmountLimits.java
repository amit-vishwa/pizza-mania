package com.pizzamania.utility;

import java.math.BigDecimal;

/** Shared storage contract for computed line totals and purchase amounts. */
public final class PurchaseAmountLimits {

    public static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private PurchaseAmountLimits() {
    }

    public static void validate(BigDecimal amount) {
        if (amount == null || amount.signum() < 0
                || amount.compareTo(MAX_AMOUNT) > 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Purchase amount must be between 0.00 and 99999999.99 with at most two decimal places");
        }
    }
}
