package com.example.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordinates idempotent refunds against accepted charges. */
public class RefundService {
    private static final int MONEY_SCALE = 2;

    private final Map<String, RefundResult> refundsByKey = new LinkedHashMap<>();
    private final Map<String, BigDecimal> chargeAmounts = new LinkedHashMap<>();

    public synchronized RefundResult refund(String refundKey, ChargeResult charge, BigDecimal amount) {
        if (refundKey == null || refundKey.isBlank()) {
            throw new IllegalArgumentException("Refund key must not be null or blank");
        }

        RefundResult existing = refundsByKey.get(refundKey);
        if (existing != null) {
            return existing;
        }
        if (charge == null || charge.chargeId() == null || charge.chargeId().isBlank()) {
            throw new IllegalArgumentException("Charge and charge ID are required");
        }
        if (charge.amount() == null || amount == null) {
            throw new IllegalArgumentException("Charge and refund amounts are required");
        }

        BigDecimal originalAmount = charge.amount().setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal refundAmount = amount.setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        if (originalAmount.signum() <= 0 || refundAmount.signum() <= 0) {
            throw new IllegalArgumentException("Charge and refund amounts must be positive");
        }

        BigDecimal knownChargeAmount = chargeAmounts.get(charge.chargeId());
        if (knownChargeAmount != null && knownChargeAmount.compareTo(originalAmount) != 0) {
            throw new IllegalArgumentException("Charge amount does not match the recorded charge");
        }

        BigDecimal refunded = refundsByKey.values().stream()
                .filter(refund -> refund.chargeId().equals(charge.chargeId()))
                .map(RefundResult::amount)
                .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE), BigDecimal::add);
        if (refunded.add(refundAmount).compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException("Refund exceeds the original charge amount");
        }

        chargeAmounts.putIfAbsent(charge.chargeId(), originalAmount);
        RefundResult result = new RefundResult(refundKey, charge.chargeId(), refundAmount);
        refundsByKey.put(refundKey, result);
        return result;
    }

    public synchronized List<RefundResult> refunds() {
        return List.copyOf(refundsByKey.values());
    }
}
