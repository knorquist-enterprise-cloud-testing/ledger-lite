package com.example.ledger;

import java.math.BigDecimal;

/** External payment processor used to create charges. */
public interface PaymentGateway {
    String charge(String customerId, BigDecimal amount);
}
