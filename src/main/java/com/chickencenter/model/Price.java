package com.chickencenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Price {
    private int id;
    private int itemId;
    private LocalDate priceDate;
    private double price;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public Price() {}

    public Price(int itemId, LocalDate priceDate, double price) {
        this.itemId = itemId;
        this.priceDate = priceDate;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public LocalDate getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDate priceDate) { this.priceDate = priceDate; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
