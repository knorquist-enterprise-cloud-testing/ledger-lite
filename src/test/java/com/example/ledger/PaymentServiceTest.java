package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {
    @Test
    void sequentialRetryReturnsOriginalCharge() {
        CountingGateway gateway = new CountingGateway(0);
        PaymentService service = new PaymentService(gateway);

        ChargeResult first = service.charge("pay-1", "customer-1", new BigDecimal("42.00"));
        ChargeResult second = service.charge("pay-1", "customer-1", new BigDecimal("42.00"));

        assertEquals(first.chargeId(), second.chargeId());
        assertEquals(1, gateway.calls());
    }

    @Test
    void concurrentRetryChargesGatewayOnlyOnce() throws Exception {
        CountingGateway gateway = new CountingGateway(25);
        PaymentService service = new PaymentService(gateway);
        int callers = 16;
        ExecutorService executor = Executors.newFixedThreadPool(callers);
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<ChargeResult>> tasks = new ArrayList<>();
        for (int i = 0; i < callers; i++) {
            tasks.add(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                return service.charge("pay-concurrent", "customer-1", new BigDecimal("42.00"));
            });
        }

        var futures = tasks.stream().map(executor::submit).toList();
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        Set<String> chargeIds = futures.stream().map(future -> {
            try {
                return future.get(5, TimeUnit.SECONDS).chargeId();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }).collect(Collectors.toSet());
        executor.shutdownNow();

        assertEquals(1, gateway.calls());
        assertEquals(1, chargeIds.size());
    }

    @Test
    void nullIdempotencyKeyIsRejected() {
        PaymentService service = new PaymentService(new CountingGateway(0));
        assertThrows(IllegalArgumentException.class, () -> service.charge(null, "customer-1", new BigDecimal("10.00")));
    }

    @Test
    void blankIdempotencyKeyIsRejected() {
        PaymentService service = new PaymentService(new CountingGateway(0));
        assertThrows(IllegalArgumentException.class, () -> service.charge("  ", "customer-1", new BigDecimal("10.00")));
    }
}
