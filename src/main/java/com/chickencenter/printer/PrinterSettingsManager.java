package com.chickencenter.printer;

import com.chickencenter.model.Account;
import com.chickencenter.service.AccountService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrinterSettingsManager {

    private final AccountService accountService;

    public PrinterSettingsManager() {
        this.accountService = new AccountService();
    }

    public String getPrinterName() {
        try {
            Account account = accountService.getAccount();
            if (account != null) {
                String name = account.getPrinterName();
                return name != null && !name.trim().isEmpty() ? name.trim() : null;
            }
        } catch (SQLException e) {
            System.err.println("[PrinterSettingsManager] Error reading printer name: " + e.getMessage());
        }
        return null;
    }

    public void savePrinterName(String printerName) {
        try {
            Account account = accountService.getAccount();
            if (account != null) {
                account.setPrinterName(printerName);
                accountService.updateAccount(account);
                System.out.println("[PrinterSettingsManager] Saved printer: \"" + printerName + "\"");
            }
        } catch (SQLException e) {
            System.err.println("[PrinterSettingsManager] Error saving printer name: " + e.getMessage());
        }
    }

    public List<String> getAvailablePrinters() {
        List<String> printers = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services != null) {
            for (PrintService ps : services) {
                printers.add(ps.getName());
            }
        }
        System.out.println("[PrinterSettingsManager] Detected " + printers.size() + " printer(s): " + printers);
        return printers;
    }

    public List<String> getAvailablePrintersWithFallback() {
        List<String> printers = getAvailablePrinters();
        if (printers.isEmpty()) {
            printers.add("No printers detected. Install printer driver.");
        }
        return printers;
    }

    public boolean isPrinterAvailable(String printerName) {
        if (printerName == null || printerName.isEmpty()) return false;
        String target = printerName.trim();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services != null) {
            for (PrintService ps : services) {
                String name = ps.getName();
                if (name.equalsIgnoreCase(target) || name.toLowerCase().contains(target.toLowerCase())) {
                    return true;
                }
            }
        }
        System.out.println("[PrinterSettingsManager] Printer \"" + printerName + "\" NOT found among available printers");
        return false;
    }
}
