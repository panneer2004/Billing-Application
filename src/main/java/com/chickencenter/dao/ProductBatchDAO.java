package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.ProductBatch;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductBatchDAO {

    public int create(ProductBatch batch) throws SQLException {
        String sql = "INSERT INTO product_batches (product_id, batch_number, from_date, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, batch.getProductId());
            pstmt.setInt(2, batch.getBatchNumber());
            pstmt.setString(3, batch.getFromDate().toString());
            pstmt.setString(4, batch.getCreatedAt().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public ProductBatch findById(int id) throws SQLException {
        String sql = "SELECT * FROM product_batches WHERE id = ?";
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

    public List<ProductBatch> findByProductId(int productId) throws SQLException {
        List<ProductBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM product_batches WHERE product_id = ? ORDER BY batch_number DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public List<ProductBatch> findAll() throws SQLException {
        List<ProductBatch> batches = new ArrayList<>();
        String sql = "SELECT pb.*, p.product_name, p.unit, v.name as vendor_name " +
                    "FROM product_batches pb " +
                    "JOIN products p ON pb.product_id = p.id " +
                    "JOIN vendors v ON p.vendor_id = v.id " +
                    "ORDER BY pb.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        }
        return batches;
    }

    public void updateToDate(int batchId, LocalDate toDate) throws SQLException {
        String sql = "UPDATE product_batches SET to_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, toDate.toString());
            pstmt.setInt(2, batchId);
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM product_batches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private ProductBatch mapResultSetToBatch(ResultSet rs) throws SQLException {
        ProductBatch batch = new ProductBatch();
        batch.setId(rs.getInt("id"));
        batch.setProductId(rs.getInt("product_id"));
        batch.setBatchNumber(rs.getInt("batch_number"));
        String fromDate = rs.getString("from_date");
        if (fromDate != null) batch.setFromDate(LocalDate.parse(fromDate));
        String toDate = rs.getString("to_date");
        if (toDate != null) batch.setToDate(LocalDate.parse(toDate));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) batch.setCreatedAt(LocalDate.parse(createdAt));
        return batch;
    }
}