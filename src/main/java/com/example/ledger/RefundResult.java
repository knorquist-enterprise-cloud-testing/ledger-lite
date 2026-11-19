package com.example.ledger;

import java.math.BigDecimal;

/** Result of a refund against an original charge. */
public record RefundResult(String refundId, String chargeId, BigDecimal amount) {
}
