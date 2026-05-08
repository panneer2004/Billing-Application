package com.chickencenter.model;

import java.time.LocalDateTime;

public class Stock {
    private int id;
    private int itemId;
    private double quantity;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public Stock() {}

    public Stock(int itemId, double quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
