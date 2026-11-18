package com.dentalclinic.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatting and parsing helpers shared by the screens. */
public final class Fmt {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Fmt() {
    }

    public static String date(LocalDate d) {
        return d == null ? "" : DATE.format(d);
    }

    public static String time(LocalTime t) {
        return t == null ? "" : TIME.format(t);
    }

    public static String money(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(value) + " " + AppConfig.currency();
    }

    /** Parses "1500", "1500.50" or "1 500,50" into a 2-decimal amount. */
    public static BigDecimal parseMoney(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Please enter an amount.");
        }
        String cleaned = text.trim().replace(" ", "").replace(",", ".");
        try {
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text.trim() + "\" is not a valid amount.");
        }
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
