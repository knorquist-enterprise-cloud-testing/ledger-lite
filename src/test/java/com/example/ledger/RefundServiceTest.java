package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RefundServiceTest {
    private final ChargeResult charge = new ChargeResult("charge-1", "customer-1", new BigDecimal("10.00"));

    @Test
    void acceptsFullRefund() {
        RefundService service = new RefundService();

        RefundResult result = service.refund("refund-1", charge, new BigDecimal("10.00"));

        assertEquals("charge-1", result.chargeId());
        assertEquals(new BigDecimal("10.00"), result.amount());
        assertEquals(List.of(result), service.refunds());
    }

    @Test
    void acceptsMultiplePartialRefundsUpToChargeAmount() {
        RefundService service = new RefundService();

        RefundResult first = service.refund("refund-1", charge, new BigDecimal("3.25"));
        RefundResult second = service.refund("refund-2", charge, new BigDecimal("6.75"));

        assertEquals(new BigDecimal("10.00"), first.amount().add(second.amount()));
        assertEquals(2, service.refunds().size());
    }

    @Test
    void rejectsRefundsThatExceedChargeAmount() {
        RefundService service = new RefundService();
        service.refund("refund-1", charge, new BigDecimal("4.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.refund("refund-2", charge, new BigDecimal("6.01")));
        assertEquals(1, service.refunds().size());
    }

    @Test
    void retryWithSameKeyReturnsTheRecordedRefund() {
        RefundService service = new RefundService();

        RefundResult first = service.refund("refund-1", charge, new BigDecimal("4.00"));
        RefundResult retry = service.refund("refund-1", charge, new BigDecimal("4.00"));

        assertSame(first, retry);
        assertEquals(List.of(first), service.refunds());
    }

    @Test
    void roundsRefundAmountsWithHalfEven() {
        RefundService service = new RefundService();

        RefundResult result = service.refund("refund-1",
                new ChargeResult("charge-2", "customer-1", new BigDecimal("1.00")),
                new BigDecimal("0.985"));

        assertEquals(new BigDecimal("0.98"), result.amount());
    }
}
