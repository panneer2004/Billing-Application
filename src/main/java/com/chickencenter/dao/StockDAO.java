package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Stock;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public int create(Stock stock) throws SQLException {
        String sql = "INSERT INTO stock (item_id, quantity, created_at, last_modified_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, stock.getItemId());
            pstmt.setDouble(2, stock.getQuantity());
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

    public Stock findById(int id) throws SQLException {
        String sql = "SELECT * FROM stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
        }
        return null;
    }

    public Stock findByItemId(int itemId) throws SQLException {
        String sql = "SELECT * FROM stock WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
        }
        return null;
    }

    public List<Stock> findAll() throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stock ORDER BY item_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public List<Stock> findLowStock(double threshold) throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stock WHERE quantity <= ? ORDER BY quantity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, threshold);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public void update(Stock stock) throws SQLException {
        String sql = "UPDATE stock SET item_id = ?, quantity = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stock.getItemId());
            pstmt.setDouble(2, stock.getQuantity());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setInt(4, stock.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateQuantity(int itemId, double newQuantity) throws SQLException {
        String sql = "UPDATE stock SET quantity = ?, last_modified_at = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newQuantity);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, itemId);
            pstmt.executeUpdate();
        }
    }

    public void addQuantity(int itemId, double quantityToAdd) throws SQLException {
        Stock stock = findByItemId(itemId);
        if (stock == null) {
            Stock newStock = new Stock(itemId, quantityToAdd);
            create(newStock);
        } else {
            double newQuantity = stock.getQuantity() + quantityToAdd;
            updateQuantity(itemId, newQuantity);
        }
    }

    public void reduceQuantity(int itemId, double quantityToReduce) throws SQLException {
        Stock stock = findByItemId(itemId);
        if (stock != null) {
            double newQuantity = stock.getQuantity() - quantityToReduce;
            if (newQuantity < 0) newQuantity = 0;
            updateQuantity(itemId, newQuantity);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteByItemId(int itemId) throws SQLException {
        String sql = "DELETE FROM stock WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.executeUpdate();
        }
    }

    private Stock mapResultSetToStock(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setId(rs.getInt("id"));
        stock.setItemId(rs.getInt("item_id"));
        stock.setQuantity(rs.getDouble("quantity"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) stock.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) stock.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return stock;
    }
}
