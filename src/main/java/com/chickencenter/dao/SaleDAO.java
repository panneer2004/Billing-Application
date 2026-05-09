package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Sale;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public int create(Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (total_amount, is_billed, sale_date, created_at, last_modified_at, payment_mode, cash_amount, gpay_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, sale.getTotalAmount());
            pstmt.setInt(2, sale.isBilled() ? 1 : 0);
            pstmt.setString(3, sale.getSaleDate().toString());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setString(6, sale.getPaymentMode() != null ? sale.getPaymentMode() : "Cash");
            pstmt.setDouble(7, sale.getCashAmount());
            pstmt.setDouble(8, sale.getGpayAmount());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Sale findById(int id) throws SQLException {
        String sql = "SELECT * FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
        }
        return null;
    }

    public List<Sale> findAll() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }
        return sales;
    }

    public List<Sale> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }
        return sales;
    }

    public void update(Sale sale) throws SQLException {
        String sql = "UPDATE sales SET total_amount = ?, is_billed = ?, sale_date = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, sale.getTotalAmount());
            pstmt.setInt(2, sale.isBilled() ? 1 : 0);
            pstmt.setString(3, sale.getSaleDate().toString());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, sale.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateTotalAmount(int saleId, double totalAmount) throws SQLException {
        String sql = "UPDATE sales SET total_amount = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, saleId);
            pstmt.executeUpdate();
        }
    }

    public void updatePaymentInfo(int saleId, String paymentMode, double cashAmount, double gpayAmount) throws SQLException {
        String sql = "UPDATE sales SET payment_mode = ?, cash_amount = ?, gpay_amount = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paymentMode);
            pstmt.setDouble(2, cashAmount);
            pstmt.setDouble(3, gpayAmount);
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, saleId);
            pstmt.executeUpdate();
        }
    }

    public void markAsBilled(int saleId, boolean isBilled) throws SQLException {
        String sql = "UPDATE sales SET is_billed = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isBilled ? 1 : 0);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, saleId);
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public double getTotalCashByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cash_amount), 0) as total FROM sales WHERE DATE(created_at) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getTotalGPayByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(gpay_amount), 0) as total FROM sales WHERE DATE(created_at) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setBilled(rs.getInt("is_billed") == 1);
        sale.setSaleDate(LocalDate.parse(rs.getString("sale_date")));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) sale.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) sale.setLastModifiedAt(LocalDateTime.parse(lastModified));
        sale.setPaymentMode(rs.getString("payment_mode"));
        sale.setCashAmount(rs.getDouble("cash_amount"));
        sale.setGpayAmount(rs.getDouble("gpay_amount"));
        return sale;
    }
}
