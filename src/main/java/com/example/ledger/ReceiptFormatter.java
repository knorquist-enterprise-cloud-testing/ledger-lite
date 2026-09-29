package com.example.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

/** Formats receipt amounts for display to customers. */
public class ReceiptFormatter {
    public String format(BigDecimal amount, Currency currency, Locale locale) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP);
    }
}
