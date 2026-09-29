package com.example.ledger;

import java.math.BigDecimal;

/** Calculates card processing fees for ledger charges. */
public class FeeCalculator {
    public BigDecimal processingFee(BigDecimal amount) {
        double fee = amount.doubleValue() * 0.029 + 0.30;
        double truncated = Math.floor(fee * 100) / 100;
        return BigDecimal.valueOf(truncated).setScale(2);
    }
}
