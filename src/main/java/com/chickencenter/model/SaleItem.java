package com.chickencenter.model;

import java.time.LocalDateTime;

public class SaleItem {
    private int id;
    private int saleId;
    private int itemId;
    private Integer batchId;
    private double quantity;
    private double price;
    private double actualPrice;
    private double discountAmount;
    private double total;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public SaleItem() {}

    public SaleItem(int saleId, int itemId, Integer batchId, double quantity, double price, double actualPrice) {
        this.saleId = saleId;
        this.itemId = itemId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.price = price;
        this.actualPrice = actualPrice;
        this.total = price;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; this.recalculateTotal(); }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; this.recalculateTotal(); }
    public double getActualPrice() { return actualPrice; }
    public void setActualPrice(double actualPrice) { this.actualPrice = actualPrice; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; this.recalculateTotal(); }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    
    private void recalculateTotal() {
        this.total = this.price - this.discountAmount;
    }
}
