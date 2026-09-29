package com.example.ledger;

import java.math.BigDecimal;

/** Result returned after a charge is accepted by the ledger. */
public record ChargeResult(String chargeId, String customerId, BigDecimal amount) {
}
