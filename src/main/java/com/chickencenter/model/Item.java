package com.chickencenter.model;

import java.time.LocalDateTime;

public class Item {
    private int id;
    private String name;
    private int vendorId;
    private String unit;
    private Integer currentBatchId;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public Item() {}

    public Item(String name, int vendorId, String unit) {
        this.name = name;
        this.vendorId = vendorId;
        this.unit = unit;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getCurrentBatchId() { return currentBatchId; }
    public void setCurrentBatchId(Integer currentBatchId) { this.currentBatchId = currentBatchId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
