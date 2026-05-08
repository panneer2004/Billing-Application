package com.chickencenter.model;

import java.time.LocalDateTime;

public class Employee {
    private int id;
    private String name;
    private String gender;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public Employee() {}

    public Employee(String name, String gender) {
        this.name = name;
        this.gender = gender;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
