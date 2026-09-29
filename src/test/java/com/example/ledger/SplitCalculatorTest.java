package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SplitCalculatorTest {
    private final SplitCalculator calculator = new SplitCalculator();

    @Test
    void hundredDollarsSplitThreeWaysKeepsEveryCent() {
        List<BigDecimal> shares = calculator.split(new BigDecimal("100.00"), 3);

        assertEquals(List.of(new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33")), shares);
        assertEquals(new BigDecimal("100.00"), shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void nickelSplitTwoWaysGivesExtraCentToFirstShare() {
        assertEquals(List.of(new BigDecimal("0.03"), new BigDecimal("0.02")), calculator.split(new BigDecimal("0.05"), 2));
    }

    @Test
    void waysMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> calculator.split(new BigDecimal("10.00"), 0));
    }
}
