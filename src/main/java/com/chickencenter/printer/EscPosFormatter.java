package com.chickencenter.printer;

public class EscPosFormatter {

    public static final int RECEIPT_WIDTH = 32;

    public static String padRight(String s, int len) {
        if (s == null) s = "";
        int actual = s.length();
        if (actual >= len) return s.substring(0, len);
        StringBuilder sb = new StringBuilder(s);
        for (int i = actual; i < len; i++) sb.append(' ');
        return sb.toString();
    }

    public static String padLeft(String s, int len) {
        if (s == null) s = "";
        int actual = s.length();
        if (actual >= len) return s.substring(0, len);
        StringBuilder sb = new StringBuilder();
        for (int i = actual; i < len; i++) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    public static String center(String s, int width) {
        if (s == null) s = "";
        int actual = s.length();
        if (actual >= width) return s.substring(0, width);
        int leftPad = (width - actual) / 2;
        int rightPad = width - actual - leftPad;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPad; i++) sb.append(' ');
        sb.append(s);
        for (int i = 0; i < rightPad; i++) sb.append(' ');
        return sb.toString();
    }

    public static String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1) + ".";
    }

    public static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    public static String dashedLine() {
        return repeat('-', RECEIPT_WIDTH);
    }

    public static String formatCurrency(double amount) {
        return String.format("%.2f", amount);
    }

    public static String formatItemRow(String name, String qtyText, String amountText) {
        name = truncate(name, 14);
        qtyText = truncate(qtyText, 8);
        amountText = truncate(amountText, 8);
        int remaining = RECEIPT_WIDTH - name.length() - qtyText.length() - amountText.length();
        if (remaining < 1) remaining = 1;
        return name + repeat(' ', remaining) + qtyText + amountText;
    }

    public static String formatTotalRow(String label, String value) {
        label = truncate(label, 18);
        value = truncate(value, 12);
        int remaining = RECEIPT_WIDTH - label.length() - value.length();
        if (remaining < 1) remaining = 1;
        return label + repeat(' ', remaining) + value;
    }

    public static String formatLine(String left, String right) {
        left = truncate(left, 18);
        right = truncate(right, 12);
        right = padLeft(right, 12);
        int remaining = RECEIPT_WIDTH - left.length() - right.length();
        if (remaining < 1) remaining = 1;
        return left + repeat(' ', remaining) + right;
    }

    public static String formatItemLine(String product, String qty, String amount) {
        product = truncate(product, 13);
        qty = truncate(qty, 6);
        amount = truncate(amount, 9);
        return String.format("%-14s %s %10s", product, center(qty, 6), amount);
    }

    public static String formatTotalLine(String label, String amount) {
        label = truncate(label, 21);
        amount = truncate(amount, 9);
        return String.format("%-22s%10s", label, amount);
    }
}
