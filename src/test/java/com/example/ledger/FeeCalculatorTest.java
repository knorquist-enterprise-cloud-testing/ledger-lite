package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FeeCalculatorTest {
    private final FeeCalculator calculator = new FeeCalculator();

    @Test
    void tenDollarFeeRoundsToFiftyNineCents() {
        assertEquals(new BigDecimal("0.59"), calculator.processingFee(new BigDecimal("10.00")));
    }

    @Test
    void nineteenNinetyNineFeeRoundsHalfEven() {
        assertEquals(new BigDecimal("0.88"), calculator.processingFee(new BigDecimal("19.99")));
    }

    @Test
    void oneHundredDollarFeeIsThreeTwenty() {
        assertEquals(new BigDecimal("3.20"), calculator.processingFee(new BigDecimal("100.00")));
    }

    @Test
    void oneCentChargeStillHasThirtyCentFixedFee() {
        assertEquals(new BigDecimal("0.30"), calculator.processingFee(new BigDecimal("0.01")));
    }
}
