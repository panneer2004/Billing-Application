package com.chickencenter.model;

import java.time.LocalDate;

public class ProductBatch {
    private int id;
    private int productId;
    private int batchNumber;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String productName;
    private String vendorName;
    private String unit;
    private LocalDate createdAt;

    public ProductBatch() {}

    public ProductBatch(int productId, int batchNumber, LocalDate fromDate) {
        this.productId = productId;
        this.batchNumber = batchNumber;
        this.fromDate = fromDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getBatchNumber() { return batchNumber; }
    public void setBatchNumber(int batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}