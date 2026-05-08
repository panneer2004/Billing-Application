package com.chickencenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Sale {
    private int id;
    private double totalAmount;
    private boolean isBilled;
    private LocalDate saleDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String paymentMode;
    private double cashAmount;
    private double gpayAmount;

    public Sale() {}

    public Sale(LocalDate saleDate) {
        this.saleDate = saleDate;
        this.totalAmount = 0;
        this.isBilled = false;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        this.paymentMode = "Cash";
        this.cashAmount = 0;
        this.gpayAmount = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public boolean isBilled() { return isBilled; }
    public void setBilled(boolean billed) { isBilled = billed; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public double getCashAmount() { return cashAmount; }
    public void setCashAmount(double cashAmount) { this.cashAmount = cashAmount; }
    public double getGpayAmount() { return gpayAmount; }
    public void setGpayAmount(double gpayAmount) { this.gpayAmount = gpayAmount; }
}
