package com.chickencenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Product {
    private int id;
    private String productName;
    private String unit;
    private int vendorId;
    private int currentBatchId;
    private double stock;
    private double price;
    private double bulkPrice;
    private double bulkThreshold;
    private int isActive = 1;
    private String productSource = "PURCHASE";
    private LocalDate createdAt;
    private LocalDateTime lastModifiedAt;

    public Product() {}
    
    public Product(String productName, String unit, int vendorId) {
        this.productName = productName;
        this.unit = unit;
        this.vendorId = vendorId;
        this.currentBatchId = 0;
        this.stock = 0;
        this.price = 0;
        this.isActive = 1;
        this.createdAt = LocalDate.now();
        this.lastModifiedAt = LocalDateTime.now();
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }
    public int getCurrentBatchId() { return currentBatchId; }
    public void setCurrentBatchId(int currentBatchId) { this.currentBatchId = currentBatchId; }
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getBulkPrice() { return bulkPrice; }
    public void setBulkPrice(double bulkPrice) { this.bulkPrice = bulkPrice; }
    public double getBulkThreshold() { return bulkThreshold; }
    public void setBulkThreshold(double bulkThreshold) { this.bulkThreshold = bulkThreshold; }
    public int getIsActive() { return isActive; }
    public void setIsActive(int isActive) { this.isActive = isActive; }
    public String getProductSource() { return productSource; }
    public void setProductSource(String productSource) { this.productSource = productSource; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    
    @Override
    public String toString() {
        return productName + " (" + unit + ")";
    }
}