package com.example.ledger;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

final class CountingGateway implements PaymentGateway {
    private final AtomicInteger calls = new AtomicInteger();
    private final long delayMillis;

    CountingGateway(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public String charge(String customerId, BigDecimal amount) {
        int call = calls.incrementAndGet();
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted", e);
            }
        }
        return "charge-" + call;
    }

    int calls() {
        return calls.get();
    }
}
