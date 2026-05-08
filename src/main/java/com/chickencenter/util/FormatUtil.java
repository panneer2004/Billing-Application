package com.chickencenter.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(dateFormatter) : "";
    }

    public static String formatDateTime(LocalDate date) {
        return date != null ? date.format(dateTimeFormatter) : "";
    }

    public static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    public static boolean isValidAmount(String amount) {
        try {
            double val = Double.parseDouble(amount);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
