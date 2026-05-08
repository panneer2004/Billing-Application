package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.SaleItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleItemDAO {

    public int create(SaleItem saleItem) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, item_id, batch_id, quantity, price, actualprice, discount_amount, total, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, saleItem.getSaleId());
            pstmt.setInt(2, saleItem.getItemId());
            if (saleItem.getBatchId() != null) {
                pstmt.setInt(3, saleItem.getBatchId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setDouble(4, saleItem.getQuantity());
            pstmt.setDouble(5, saleItem.getPrice());
            pstmt.setDouble(6, saleItem.getActualPrice());
            pstmt.setDouble(7, saleItem.getDiscountAmount());
            pstmt.setDouble(8, saleItem.getTotal());
            pstmt.setString(9, LocalDateTime.now().toString());
            pstmt.setString(10, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public SaleItem findById(int id) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSaleItem(rs);
            }
        }
        return null;
    }

    public double getTotalSoldQuantity(int itemId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM sale_items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public double getTotalSoldQuantityByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(si.quantity), 0) FROM sale_items si JOIN sales s ON si.sale_id = s.id WHERE DATE(s.created_at) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public List<SaleItem> findBySaleId(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }
        }
        return items;
    }

    public List<SaleItem> findAll() throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }
        }
        return items;
    }

    public void update(SaleItem saleItem) throws SQLException {
        String sql = "UPDATE sale_items SET sale_id = ?, item_id = ?, batch_id = ?, quantity = ?, price = ?, actualprice = ?, discount_amount = ?, total = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleItem.getSaleId());
            pstmt.setInt(2, saleItem.getItemId());
            if (saleItem.getBatchId() != null) {
                pstmt.setInt(3, saleItem.getBatchId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setDouble(4, saleItem.getQuantity());
            pstmt.setDouble(5, saleItem.getPrice());
            pstmt.setDouble(6, saleItem.getActualPrice());
            pstmt.setDouble(7, saleItem.getDiscountAmount());
            pstmt.setDouble(8, saleItem.getTotal());
            pstmt.setString(9, LocalDateTime.now().toString());
            pstmt.setInt(10, saleItem.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sale_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteBySaleId(int saleId) throws SQLException {
        String sql = "DELETE FROM sale_items WHERE sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            pstmt.executeUpdate();
        }
    }

    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        SaleItem item = new SaleItem();
        item.setId(rs.getInt("id"));
        item.setSaleId(rs.getInt("sale_id"));
        item.setItemId(rs.getInt("item_id"));
        int batchId = rs.getInt("batch_id");
        item.setBatchId(rs.wasNull() ? null : batchId);
        item.setQuantity(rs.getDouble("quantity"));
        item.setPrice(rs.getDouble("price"));
        item.setActualPrice(rs.getDouble("actualprice"));
        item.setDiscountAmount(rs.getDouble("discount_amount"));
        item.setTotal(rs.getDouble("total"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) item.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) item.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return item;
    }
}
