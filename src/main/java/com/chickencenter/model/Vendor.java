package com.chickencenter.model;

import java.time.LocalDateTime;

public class Vendor {
    private int id;
    private String name;
    private String contactNumber;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public Vendor() {}

    public Vendor(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    
    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
