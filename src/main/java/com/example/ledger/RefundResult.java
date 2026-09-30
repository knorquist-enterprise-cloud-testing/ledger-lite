package com.example.ledger;

import java.math.BigDecimal;

/** Result recorded after a refund is accepted by the ledger. */
public record RefundResult(String refundKey, String chargeId, BigDecimal amount) {
}
