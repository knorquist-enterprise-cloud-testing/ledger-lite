package com.example.ledger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/** Formats receipt amounts for display to customers. */
public class ReceiptFormatter {
    public String format(BigDecimal amount, Currency currency, Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);
        int fractionDigits = currency.getDefaultFractionDigits();
        formatter.setMinimumFractionDigits(fractionDigits);
        formatter.setMaximumFractionDigits(fractionDigits);
        return formatter.format(amount);
    }
}
