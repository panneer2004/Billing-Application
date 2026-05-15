package com.chickencenter.model;

public class Purchase {
    private int id;
    private int itemBatchId;
    private int itemId;
    private int vendorId;
    private double batchQuantity;
    private double balanceQuantity;
    private double rate;
    private double totalAmount;
    private String createdAt;
    private String lastModifiedAt;

    public Purchase() {}

    public Purchase(int itemId, int vendorId, double batchQuantity, double rate, double totalAmount) {
        this.itemId = itemId;
        this.vendorId = vendorId;
        this.batchQuantity = batchQuantity;
        this.balanceQuantity = batchQuantity;
        this.rate = rate;
        this.totalAmount = totalAmount;
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
    public double getBalanceQuantity() { return balanceQuantity; }
    public void setBalanceQuantity(double balanceQuantity) { this.balanceQuantity = balanceQuantity; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(String lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}