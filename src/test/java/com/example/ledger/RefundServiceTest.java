package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RefundServiceTest {
    private final ChargeResult charge = new ChargeResult("charge-1", "customer-1", new BigDecimal("100.00"));
    private final RefundService service = new RefundService(List.of(charge));

    @Test
    void fullRefundUsesOriginalChargeIdAndExhaustsBalance() {
        RefundResult refund = service.refund("refund-1", charge.chargeId(), new BigDecimal("100.00"));

        assertEquals(charge.chargeId(), refund.chargeId());
        assertEquals(new BigDecimal("100.00"), refund.amount());
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("refund-2", charge.chargeId(), new BigDecimal("0.01")));
    }

    @Test
    void multiplePartialRefundsCanExhaustBalanceExactly() {
        service.refund("refund-1", charge.chargeId(), new BigDecimal("33.33"));
        service.refund("refund-2", charge.chargeId(), new BigDecimal("33.33"));
        RefundResult last = service.refund("refund-3", charge.chargeId(), new BigDecimal("33.34"));

        assertEquals(new BigDecimal("33.34"), last.amount());
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("refund-4", charge.chargeId(), new BigDecimal("0.01")));
    }

    @Test
    void overRefundDoesNotReserveAnyBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("too-much", charge.chargeId(), new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"),
                service.refund("valid", charge.chargeId(), new BigDecimal("100.00")).amount());
    }

    @Test
    void retryReturnsSameRefundWithoutConsumingBalanceAgain() {
        RefundResult first = service.refund("refund-1", charge.chargeId(), new BigDecimal("75.00"));
        assertSame(first, service.refund("refund-1", charge.chargeId(), new BigDecimal("75.00")));
        service.refund("refund-2", charge.chargeId(), new BigDecimal("25.00"));
        assertSame(first, service.refund("refund-1", charge.chargeId(), new BigDecimal("75.00")));
    }

    @Test
    void concurrentRetryRecordsOnlyOneRefund() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            CountDownLatch start = new CountDownLatch(1);
            var futures = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(i -> executor.submit(() -> {
                        start.await(5, TimeUnit.SECONDS);
                        return service.refund("same-key", charge.chargeId(), new BigDecimal("75.00"));
                    })).toList();
            start.countDown();
            RefundResult first = futures.getFirst().get(5, TimeUnit.SECONDS);
            for (var future : futures) {
                assertSame(first, future.get(5, TimeUnit.SECONDS));
            }
        }
        assertEquals(new BigDecimal("25.00"),
                service.refund("remaining", charge.chargeId(), new BigDecimal("25.00")).amount());
    }

    @Test
    void concurrentDifferentKeysCannotOverRefund() throws Exception {
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            var futures = java.util.stream.IntStream.range(0, 2)
                    .mapToObj(i -> executor.submit(() -> {
                        start.await(5, TimeUnit.SECONDS);
                        try {
                            service.refund("key-" + i, charge.chargeId(), new BigDecimal("75.00"));
                            accepted.incrementAndGet();
                        } catch (IllegalArgumentException overRefund) {
                            rejected.incrementAndGet();
                        }
                        return null;
                    })).toList();
            start.countDown();
            for (var future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        }
        assertEquals(1, accepted.get());
        assertEquals(1, rejected.get());
        service.refund("remaining", charge.chargeId(), new BigDecimal("25.00"));
    }

    @Test
    void rejectsInvalidRequestsAndRoundsHalfEven() {
        assertThrows(IllegalArgumentException.class,
                () -> service.refund(null, charge.chargeId(), new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("  ", charge.chargeId(), new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("unknown", "missing", new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("zero", charge.chargeId(), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("negative", charge.chargeId(), new BigDecimal("-1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.refund("tiny", charge.chargeId(), new BigDecimal("0.001")));
        assertEquals(new BigDecimal("1.00"),
                service.refund("rounded", charge.chargeId(), new BigDecimal("1.005")).amount());
    }

    @Test
    void refundsCanExhaustChargeWithFractionalCentPrecision() {
        RefundService preciseService = new RefundService(List.of(
                new ChargeResult("precise", "customer-1", new BigDecimal("1.005"))));

        preciseService.refund("first", "precise", new BigDecimal("0.500"));
        assertEquals(new BigDecimal("0.505"),
                preciseService.refund("last", "precise", new BigDecimal("0.505")).amount());
        assertThrows(IllegalArgumentException.class,
                () -> preciseService.refund("excess", "precise", new BigDecimal("0.001")));
    }
}
