package com.pizzamania.utility;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseAmountLimitsTest {

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "0.01", "1500.00", "99999999.99", "1.2300"})
    void supportedAmountsAreAcceptedWithoutRounding(String amount) {
        assertDoesNotThrow(() -> PurchaseAmountLimits.validate(new BigDecimal(amount)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100000000.00", "0.001"})
    void unsupportedAmountsAreRejected(String amount) {
        assertThrows(IllegalArgumentException.class,
                () -> PurchaseAmountLimits.validate(new BigDecimal(amount)));
    }

    @Test
    void missingAmountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseAmountLimits.validate(null));
    }
}
