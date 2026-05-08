package com.chickencenter.util;

import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceExporter {

    public static void exportInvoice(Sale sale, List<SaleItem> items, String itemNames[], String filePath, String customerName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoice");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 22);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subFont = workbook.createFont();
            subFont.setFontHeightInPoints((short) 11);
            subtitleStyle.setFont(subFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle altCellStyle = workbook.createCellStyle();
            altCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            altCellStyle.setBorderBottom(BorderStyle.THIN);
            altCellStyle.setBorderTop(BorderStyle.THIN);
            altCellStyle.setBorderLeft(BorderStyle.THIN);
            altCellStyle.setBorderRight(BorderStyle.THIN);
            altCellStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            Font totalLabelFont = workbook.createFont();
            totalLabelFont.setBold(true);
            totalLabelFont.setFontHeightInPoints((short) 14);
            totalLabelStyle.setFont(totalLabelFont);
            totalLabelStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelStyle.setBorderTop(BorderStyle.MEDIUM);

            CellStyle totalValueStyle = workbook.createCellStyle();
            Font totalValueFont = workbook.createFont();
            totalValueFont.setBold(true);
            totalValueFont.setFontHeightInPoints((short) 14);
            totalValueFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            totalValueStyle.setFont(totalValueFont);
            totalValueStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
            totalValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalValueStyle.setBorderTop(BorderStyle.MEDIUM);

            int rowNum = 0;

            Row companyRow = sheet.createRow(rowNum++);
            Cell companyCell = companyRow.createCell(0);
            companyCell.setCellValue("JK CHICKEN CENTER");
            companyCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

            Row addressRow = sheet.createRow(rowNum++);
            Cell addressCell = addressRow.createCell(0);
            addressCell.setCellValue("Fresh Poultry & Meat Supplier");
            addressCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

            Row invoiceRow = sheet.createRow(rowNum++);
            Cell invoiceCell = invoiceRow.createCell(0);
            invoiceCell.setCellValue("INVOICE #" + sale.getId());
            invoiceCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Date: " + sale.getSaleDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            dateCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

            if (customerName != null && !customerName.isEmpty()) {
                Row customerRow = sheet.createRow(rowNum++);
                Cell customerCell = customerRow.createCell(0);
                customerCell.setCellValue("Customer: " + customerName);
                customerCell.setCellStyle(subtitleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));
            }

            rowNum++;

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Sr.", "Product", "Quantity", "Unit", "Rate (Rs.)", "Amount (Rs.)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            double totalAmount = 0;
            int srNo = 1;
            boolean alternate = false;
            for (SaleItem item : items) {
                Row row = sheet.createRow(rowNum++);
                CellStyle rowStyle = alternate ? altCellStyle : cellStyle;

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(srNo++);
                cell0.setCellStyle(numberStyle);

                Cell cell1 = row.createCell(1);
                String itemName = "";
                for (SaleItem si : items) {
                    if (item.getItemId() == si.getItemId()) {
                        for (String name : itemNames) {
                            itemName = name;
                            break;
                        }
                    }
                }
                cell1.setCellValue(itemName);
                cell1.setCellStyle(rowStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(item.getQuantity());
                cell2.setCellStyle(numberStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue("");
                cell3.setCellStyle(rowStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(String.format("%.2f", item.getPrice()));
                cell4.setCellStyle(numberStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(String.format("%.2f", item.getTotal()));
                cell5.setCellStyle(numberStyle);

                totalAmount += item.getTotal();
                alternate = !alternate;
            }

            rowNum++;

            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabel = totalRow.createCell(4);
            totalLabel.setCellValue("GRAND TOTAL:");
            totalLabel.setCellStyle(totalLabelStyle);

            Cell totalValue = totalRow.createCell(5);
            totalValue.setCellValue(String.format("%.2f", totalAmount));
            totalValue.setCellStyle(totalValueStyle);

            rowNum += 2;

            Row footerRow = sheet.createRow(rowNum);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("Thank you for your business!");
            CellStyle footerStyle = workbook.createCellStyle();
            footerStyle.setAlignment(HorizontalAlignment.CENTER);
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5));

            Row poweredRow = sheet.createRow(rowNum + 1);
            Cell poweredCell = poweredRow.createCell(0);
            poweredCell.setCellValue("Generated by JK Chicken Center Billing System");
            CellStyle poweredStyle = workbook.createCellStyle();
            Font poweredFont = workbook.createFont();
            poweredFont.setFontHeightInPoints((short) 9);
            poweredFont.setItalic(true);
            poweredStyle.setFont(poweredFont);
            poweredStyle.setAlignment(HorizontalAlignment.CENTER);
            poweredCell.setCellStyle(poweredStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 5));

            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 12000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 6000);
            sheet.setColumnWidth(5, 8000);

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }
}
