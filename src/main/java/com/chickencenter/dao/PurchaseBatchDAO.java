package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.PurchaseBatch;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseBatchDAO {

    public int create(PurchaseBatch batch) throws SQLException {
        String sql = "INSERT INTO purchase_batches (item_batch_id, item_id, vendor_id, batch_quantity, total_amount, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, batch.getItemBatchId());
            pstmt.setInt(2, batch.getItemId());
            pstmt.setInt(3, batch.getVendorId());
            pstmt.setDouble(4, batch.getBatchQuantity());
            pstmt.setDouble(5, batch.getTotalAmount());
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.setString(7, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public PurchaseBatch findById(int id) throws SQLException {
        String sql = "SELECT * FROM purchase_batches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBatch(rs);
            }
        }
        return null;
    }

    public List<PurchaseBatch> findAll() throws SQLException {
        List<PurchaseBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM purchase_batches ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public List<PurchaseBatch> findByItemId(int itemId) throws SQLException {
        List<PurchaseBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM purchase_batches WHERE item_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public List<PurchaseBatch> findByVendorId(int vendorId) throws SQLException {
        List<PurchaseBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM purchase_batches WHERE vendor_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public void update(PurchaseBatch batch) throws SQLException {
        String sql = "UPDATE purchase_batches SET item_batch_id = ?, item_id = ?, vendor_id = ?, batch_quantity = ?, total_amount = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batch.getItemBatchId());
            pstmt.setInt(2, batch.getItemId());
            pstmt.setInt(3, batch.getVendorId());
            pstmt.setDouble(4, batch.getBatchQuantity());
            pstmt.setDouble(5, batch.getTotalAmount());
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.setInt(7, batch.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM purchase_batches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public double getTotalPurchaseAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM purchase_batches";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public List<PurchaseBatch> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<PurchaseBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM purchase_batches WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public double getTotalPurchaseAmountByDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM purchase_batches WHERE created_at BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    private PurchaseBatch mapResultSetToBatch(ResultSet rs) throws SQLException {
        PurchaseBatch batch = new PurchaseBatch();
        batch.setId(rs.getInt("id"));
        batch.setItemBatchId(rs.getInt("item_batch_id"));
        batch.setItemId(rs.getInt("item_id"));
        batch.setVendorId(rs.getInt("vendor_id"));
        batch.setBatchQuantity(rs.getDouble("batch_quantity"));
        batch.setTotalAmount(rs.getDouble("total_amount"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) batch.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) batch.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return batch;
    }
}
