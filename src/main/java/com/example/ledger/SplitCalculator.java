package com.example.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Splits a total amount into equal ledger shares. */
public class SplitCalculator {
    public List<BigDecimal> split(BigDecimal total, int ways) {
        if (ways <= 0) {
            throw new IllegalArgumentException("ways must be positive");
        }
        BigDecimal share = total.divide(BigDecimal.valueOf(ways), 2, RoundingMode.HALF_UP);
        List<BigDecimal> shares = new ArrayList<>();
        for (int i = 0; i < ways; i++) {
            shares.add(share);
        }
        return shares;
    }
}
