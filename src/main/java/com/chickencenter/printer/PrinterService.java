package com.chickencenter.printer;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.util.ArrayList;
import java.util.List;

public class PrinterService {

    public void printReceipt(byte[] receiptData, String printerName) throws PrintException {
        if (receiptData == null || receiptData.length == 0) {
            throw new PrintException("No receipt data to print");
        }
        if (printerName == null || printerName.trim().isEmpty()) {
            throw new PrintException("No printer selected");
        }

        List<String> available = getAvailablePrinters();
        System.out.println("[PrinterService] Available printers (" + available.size() + "): " + available);

        PrintService printer = findPrinter(printerName);
        if (printer == null) {
            System.out.println("[PrinterService] Printer \"" + printerName + "\" NOT FOUND among " + available.size() + " available");
            throw new PrintException("Printer \"" + printerName + "\" not found. Available: " + (available.isEmpty() ? "none — install a printer driver" : String.join(", ", available)));
        }

        System.out.println("[PrinterService] Found printer: \"" + printer.getName() + "\", sending " + receiptData.length + " bytes");
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(receiptData, flavor, null);
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        DocPrintJob job = printer.createPrintJob();
        job.print(doc, attrs);
        System.out.println("[PrinterService] Print job submitted successfully");
    }

    public boolean isPrinterAvailable(String printerName) {
        if (printerName == null || printerName.trim().isEmpty()) return false;
        return findPrinter(printerName) != null;
    }

    public List<String> getAvailablePrinters() {
        List<String> names = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services != null) {
            for (PrintService ps : services) {
                names.add(ps.getName());
            }
        }
        return names;
    }

    public List<String> getAvailablePrintersWithFallbackMessage() {
        List<String> printers = getAvailablePrinters();
        if (printers.isEmpty()) {
            printers.add("No printers detected");
        }
        return printers;
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services == null || services.length == 0) {
            System.out.println("[PrinterService] PrintServiceLookup returned null/empty — no printers available");
            return null;
        }

        String target = printerName.trim();
        System.out.println("[PrinterService] Searching for printer containing: \"" + target + "\" among " + services.length + " services");

        for (PrintService ps : services) {
            String name = ps.getName();
            System.out.println("[PrinterService]   Available: \"" + name + "\"");
            if (name.equalsIgnoreCase(target) || name.toLowerCase().contains(target.toLowerCase())) {
                System.out.println("[PrinterService]   => MATCH");
                return ps;
            }
        }

        System.out.println("[PrinterService]   No match for \"" + target + "\"");
        return null;
    }

    public String detectFirstThermalPrinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services != null) {
            for (PrintService ps : services) {
                String name = ps.getName().toLowerCase();
                if (name.contains("thermal") || name.contains("pos") || name.contains("receipt")
                        || name.contains("epson") || name.contains("tm-") || name.contains("80mm")) {
                    return ps.getName();
                }
            }
        }
        return null;
    }
}
