package com.example.ledger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Coordinates idempotent customer charges through a payment gateway. */
public class PaymentService {
    private final PaymentGateway gateway;
    private final Map<String, ChargeResult> chargesByKey = new HashMap<>();

    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public ChargeResult charge(String idempotencyKey, String customerId, BigDecimal amount) {
        ChargeResult existing = chargesByKey.get(idempotencyKey);
        if (existing != null) {
            return existing;
        }
        String chargeId = gateway.charge(customerId, amount);
        ChargeResult result = new ChargeResult(chargeId, customerId, amount);
        chargesByKey.put(idempotencyKey, result);
        return result;
    }
}
