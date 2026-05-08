package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Vendor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendorDAO {

    public int create(Vendor vendor) throws SQLException {
        String sql = "INSERT INTO vendors (name, contact_number, created_at, last_modified_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, vendor.getName());
            pstmt.setString(2, vendor.getContactNumber());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Vendor findById(int id) throws SQLException {
        String sql = "SELECT * FROM vendors WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToVendor(rs);
            }
        }
        return null;
    }

    public List<Vendor> findAll() throws SQLException {
        List<Vendor> vendors = new ArrayList<>();
        String sql = "SELECT * FROM vendors ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vendors.add(mapResultSetToVendor(rs));
            }
        }
        return vendors;
    }

    public void update(Vendor vendor) throws SQLException {
        String sql = "UPDATE vendors SET name = ?, contact_number = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vendor.getName());
            pstmt.setString(2, vendor.getContactNumber());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setInt(4, vendor.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vendors WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Vendor> searchByName(String name) throws SQLException {
        List<Vendor> vendors = new ArrayList<>();
        String sql = "SELECT * FROM vendors WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vendors.add(mapResultSetToVendor(rs));
            }
        }
        return vendors;
    }

    private Vendor mapResultSetToVendor(ResultSet rs) throws SQLException {
        Vendor vendor = new Vendor();
        vendor.setId(rs.getInt("id"));
        vendor.setName(rs.getString("name"));
        vendor.setContactNumber(rs.getString("contact_number"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) vendor.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) vendor.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return vendor;
    }
}
