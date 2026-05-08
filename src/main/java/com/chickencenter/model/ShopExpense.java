package com.chickencenter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShopExpense {
    private int id;
    private String note;
    private double amount;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public ShopExpense() {}

    public ShopExpense(String note, double amount, LocalDate expenseDate) {
        this.note = note;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}