package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Price;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PriceDAO {

    public int create(Price price) throws SQLException {
        String sql = "INSERT INTO price_list (item_id, price_date, price, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, price.getItemId());
            pstmt.setString(2, price.getPriceDate().toString());
            pstmt.setDouble(3, price.getPrice());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Price findById(int id) throws SQLException {
        String sql = "SELECT * FROM price_list WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPrice(rs);
            }
        }
        return null;
    }

    public List<Price> findAll() throws SQLException {
        List<Price> prices = new ArrayList<>();
        String sql = "SELECT * FROM price_list ORDER BY price_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    public Price findLatestByItemId(int itemId) throws SQLException {
        String sql = "SELECT * FROM price_list WHERE item_id = ? ORDER BY price_date DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPrice(rs);
            }
        }
        return null;
    }

    public List<Price> findByItemId(int itemId) throws SQLException {
        List<Price> prices = new ArrayList<>();
        String sql = "SELECT * FROM price_list WHERE item_id = ? ORDER BY price_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    public void update(Price price) throws SQLException {
        String sql = "UPDATE price_list SET item_id = ?, price_date = ?, price = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, price.getItemId());
            pstmt.setString(2, price.getPriceDate().toString());
            pstmt.setDouble(3, price.getPrice());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, price.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM price_list WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void setPriceForItem(int itemId, double newPrice) throws SQLException {
        Price existing = findLatestByItemId(itemId);
        if (existing != null && existing.getPrice() == newPrice) {
            return;
        }
        Price price = new Price(itemId, LocalDate.now(), newPrice);
        create(price);
    }

    private Price mapResultSetToPrice(ResultSet rs) throws SQLException {
        Price price = new Price();
        price.setId(rs.getInt("id"));
        price.setItemId(rs.getInt("item_id"));
        price.setPriceDate(LocalDate.parse(rs.getString("price_date")));
        price.setPrice(rs.getDouble("price"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) price.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) price.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return price;
    }
}
