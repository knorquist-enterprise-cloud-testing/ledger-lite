package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ReceiptFormatterTest {
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void formatsUsdForUnitedStatesLocale() {
        assertEquals("$1,234.50", formatter.format(new BigDecimal("1234.50"), Currency.getInstance("USD"), Locale.US));
    }

    @Test
    void formatsJpyWithoutFractionDigits() {
        assertEquals("¥1,234", formatter.format(new BigDecimal("1234"), Currency.getInstance("JPY"), Locale.US));
    }

    @Test
    void formatsEuroForGermanLocale() {
        assertEquals("1.234,50\u00A0€", formatter.format(new BigDecimal("1234.50"), Currency.getInstance("EUR"), Locale.GERMANY));
    }
}
