package com.example.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Records idempotent refunds against known charges. */
public class RefundService {
    private final Map<String, BigDecimal> chargeAmounts = new HashMap<>();
    private final Map<String, BigDecimal> refundedAmounts = new HashMap<>();
    private final Map<String, RefundResult> refundsByKey = new HashMap<>();

    public RefundService(Collection<ChargeResult> charges) {
        for (ChargeResult charge : charges) {
            if (charge == null || charge.chargeId() == null || charge.chargeId().isBlank()
                    || charge.amount() == null || charge.amount().signum() <= 0
                    || chargeAmounts.putIfAbsent(charge.chargeId(), charge.amount()) != null) {
                throw new IllegalArgumentException("charges must have distinct IDs and positive amounts");
            }
        }
    }

    public synchronized RefundResult refund(String refundKey, String chargeId, BigDecimal amount) {
        if (refundKey == null || refundKey.isBlank()) {
            throw new IllegalArgumentException("refund key must not be blank");
        }
        RefundResult existing = refundsByKey.get(refundKey);
        if (existing != null) {
            return existing;
        }
        BigDecimal originalAmount = chargeAmounts.get(chargeId);
        if (originalAmount == null) {
            throw new IllegalArgumentException("unknown charge ID");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("refund amount must be positive");
        }
        BigDecimal roundedAmount = amount.setScale(Math.max(2, originalAmount.scale()), RoundingMode.HALF_EVEN);
        if (roundedAmount.signum() <= 0) {
            throw new IllegalArgumentException("refund amount is too small");
        }
        BigDecimal total = refundedAmounts.getOrDefault(chargeId, BigDecimal.ZERO).add(roundedAmount);
        if (total.compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException("refund exceeds original charge amount");
        }

        RefundResult result = new RefundResult(UUID.randomUUID().toString(), chargeId, roundedAmount);
        refundedAmounts.put(chargeId, total);
        refundsByKey.put(refundKey, result);
        return result;
    }
}
