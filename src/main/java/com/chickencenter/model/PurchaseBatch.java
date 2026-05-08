package com.chickencenter.model;

import java.time.LocalDateTime;

public class PurchaseBatch {
    private int id;
    private int itemBatchId;
    private int itemId;
    private int vendorId;
    private double batchQuantity;
    private double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public PurchaseBatch() {}

    public PurchaseBatch(int itemId, int vendorId, double batchQuantity, double totalAmount) {
        this.itemId = itemId;
        this.vendorId = vendorId;
        this.batchQuantity = batchQuantity;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getItemBatchId() { return itemBatchId; }
    public void setItemBatchId(int itemBatchId) { this.itemBatchId = itemBatchId; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }
    public double getBatchQuantity() { return batchQuantity; }
    public void setBatchQuantity(double batchQuantity) { this.batchQuantity = batchQuantity; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
