package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Item;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public int create(Item item) throws SQLException {
        String sql = "INSERT INTO items (name, vendor_id, unit, current_batch_id, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getVendorId());
            pstmt.setString(3, item.getUnit());
            if (item.getCurrentBatchId() != null) {
                pstmt.setInt(4, item.getCurrentBatchId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Item findById(int id) throws SQLException {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        }
        return null;
    }

    public List<Item> findAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public List<Item> findByVendorId(int vendorId) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE vendor_id = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public void update(Item item) throws SQLException {
        String sql = "UPDATE items SET name = ?, vendor_id = ?, unit = ?, current_batch_id = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getVendorId());
            pstmt.setString(3, item.getUnit());
            if (item.getCurrentBatchId() != null) {
                pstmt.setInt(4, item.getCurrentBatchId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setInt(6, item.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Item> searchByName(String name) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setVendorId(rs.getInt("vendor_id"));
        item.setUnit(rs.getString("unit"));
        int batchId = rs.getInt("current_batch_id");
        item.setCurrentBatchId(rs.wasNull() ? null : batchId);
        String createdAt = rs.getString("created_at");
        if (createdAt != null) item.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) item.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return item;
    }
}
