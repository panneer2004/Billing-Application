package com.chickencenter.printer;

import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;
import com.github.anastaciocintra.escpos.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ThermalReceiptBuilder {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    public byte[] buildReceipt(Sale sale, List<SaleItem> items, String shopName, String shopAddress, String shopPhone,
                                Map<Integer, String> productNames) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        EscPos escpos = new EscPos(baos);

        escpos.writeLF(EscPosFormatter.center(shopName != null ? shopName : "JK CHICKEN CENTER", EscPosFormatter.RECEIPT_WIDTH));
        escpos.writeLF("");

        String dt = sale.getCreatedAt() != null
                ? sale.getCreatedAt().format(DT_FMT)
                : LocalDateTime.now().format(DT_FMT);
        escpos.writeLF("Bill No : " + sale.getId());
        escpos.writeLF("Date    : " + dt);
        escpos.writeLF("");
        escpos.writeLF(EscPosFormatter.dashedLine());
        escpos.writeLF("");

        for (SaleItem item : items) {
            String name = productNames != null ? productNames.getOrDefault(item.getItemId(), "Item #" + item.getItemId()) : "Item #" + item.getItemId();
            String qtyText = formatQty(item);
            String amtText = EscPosFormatter.formatCurrency(item.getTotal());
            escpos.writeLF(EscPosFormatter.formatItemLine(name, qtyText, amtText));
        }

        escpos.writeLF("");
        escpos.writeLF(EscPosFormatter.dashedLine());
        escpos.writeLF(EscPosFormatter.formatTotalLine("TOTAL", EscPosFormatter.formatCurrency(sale.getTotalAmount())));
        escpos.writeLF(EscPosFormatter.dashedLine());

        escpos.feed(3);
        escpos.cut(EscPos.CutMode.FULL);
        escpos.close();

        return baos.toByteArray();
    }

    private String formatQty(SaleItem item) {
        double qty = item.getQuantity();
        if (qty == (long) qty) return String.valueOf((int) qty);
        return String.format("%.2f", qty);
    }

    public byte[] buildTestReceipt(String shopName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        EscPos escpos = new EscPos(baos);

        String line = EscPosFormatter.center("TEST PRINT", EscPosFormatter.RECEIPT_WIDTH);
        escpos.writeLF(line);
        escpos.writeLF("");
        escpos.writeLF(EscPosFormatter.center(shopName != null ? shopName : "JK CHICKEN CENTER", EscPosFormatter.RECEIPT_WIDTH));
        escpos.writeLF("");
        escpos.writeLF(EscPosFormatter.center("Printer Connected Successfully", EscPosFormatter.RECEIPT_WIDTH));
        escpos.writeLF("");
        escpos.writeLF(EscPosFormatter.center("If you can read this,", EscPosFormatter.RECEIPT_WIDTH));
        escpos.writeLF(EscPosFormatter.center("the printer is working correctly.", EscPosFormatter.RECEIPT_WIDTH));
        escpos.writeLF("");

        escpos.feed(3);
        escpos.cut(EscPos.CutMode.FULL);
        escpos.close();

        return baos.toByteArray();
    }

}
