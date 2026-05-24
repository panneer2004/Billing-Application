package com.chickencenter.printer;

import javafx.concurrent.Task;

import javax.print.PrintException;
import java.util.function.Consumer;

public class ReceiptPrintTask extends Task<Void> {

    private final byte[] receiptData;
    private final String printerName;
    private final Consumer<String> onSuccess;
    private final Consumer<String> onError;

    public ReceiptPrintTask(byte[] receiptData, String printerName,
                            Consumer<String> onSuccess, Consumer<String> onError) {
        this.receiptData = receiptData;
        this.printerName = printerName;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    protected Void call() throws Exception {
        PrinterSettingsManager psm = new PrinterSettingsManager();
        String resolvedPrinter = printerName;
        if (resolvedPrinter == null || resolvedPrinter.trim().isEmpty()) {
            resolvedPrinter = psm.getPrinterName();
        }

        System.out.println("[ReceiptPrintTask] Starting print job for printer: \"" + resolvedPrinter + "\"");

        PrinterService printerService = new PrinterService();
        printerService.printReceipt(receiptData, resolvedPrinter);
        System.out.println("[ReceiptPrintTask] Print completed successfully");
        return null;
    }

    @Override
    protected void succeeded() {
        System.out.println("[ReceiptPrintTask] Task succeeded");
        if (onSuccess != null) {
            onSuccess.accept("Bill sent to printer successfully");
        }
    }

    @Override
    protected void failed() {
        Throwable ex = getException();
        String msg = ex != null ? ex.getMessage() : "Printing failed";
        System.err.println("[ReceiptPrintTask] Task failed: " + msg);
        if (ex != null) {
            ex.printStackTrace();
        }
        if (onError != null) {
            onError.accept(msg);
        }
    }
}
