package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Purchase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public int create(Purchase purchase) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            int nextBatchId = getNextBatchId(conn, purchase.getItemId());
            System.out.println("Next batch ID for item " + purchase.getItemId() + ": " + nextBatchId);
            
            String sql = "INSERT INTO purchases (item_batch_id, item_id, vendor_id, batch_quantity, rate, total_amount, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            System.out.println("SQL: " + sql);
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, nextBatchId);
            pstmt.setInt(2, purchase.getItemId());
            pstmt.setInt(3, purchase.getVendorId());
            pstmt.setDouble(4, purchase.getBatchQuantity());
            pstmt.setDouble(5, purchase.getRate());
            pstmt.setDouble(6, purchase.getTotalAmount());
            String now = LocalDateTime.now().toString();
            pstmt.setString(7, now);
            pstmt.setString(8, now);
            
            System.out.println("Executing update with params:");
            System.out.println("  item_batch_id: " + nextBatchId);
            System.out.println("  item_id: " + purchase.getItemId());
            System.out.println("  vendor_id: " + purchase.getVendorId());
            System.out.println("  batch_quantity: " + purchase.getBatchQuantity());
            System.out.println("  total_amount: " + purchase.getTotalAmount());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Generated ID: " + id);
                    pstmt.close();
                    conn.close();
                    return id;
                }
            }
            
            pstmt.close();
            conn.close();
            return -1;
        } catch (SQLException e) {
            System.out.println("SQL Error in create: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) {}
            }
            throw e;
        }
    }

    private int getNextBatchId(Connection conn, int itemId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(item_batch_id), 0) FROM purchases WHERE item_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, itemId);
        ResultSet rs = pstmt.executeQuery();
        int result = 1;
        if (rs.next()) {
            result = rs.getInt(1) + 1;
        }
        rs.close();
        pstmt.close();
        System.out.println("Current max batch for item " + itemId + " was " + (result - 1) + ", next will be " + result);
        return result;
    }

    public List<ProductWithVendor> findAllWithVendor() throws SQLException {
        List<ProductWithVendor> list = new ArrayList<>();
        String sql = """
            SELECT p.id, p.product_name, p.unit, p.vendor_id, p.current_batch_id, v.name as vendor_name
            FROM products p
            LEFT JOIN vendors v ON p.vendor_id = v.id
            ORDER BY p.product_name
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProductWithVendor p = new ProductWithVendor();
                p.setId(rs.getInt("id"));
                p.setProductName(rs.getString("product_name"));
                p.setUnit(rs.getString("unit"));
                p.setVendorId(rs.getInt("vendor_id"));
                p.setVendorName(rs.getString("vendor_name"));
                p.setCurrentBatchId(rs.getInt("current_batch_id"));
                list.add(p);
            }
        }
        return list;
    }

    public List<Purchase> findAll() throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                purchases.add(mapResultSetToPurchase(rs));
            }
        }
        return purchases;
    }

    public List<PurchaseWithDetails> findAllWithDetails() throws SQLException {
        List<PurchaseWithDetails> list = new ArrayList<>();
        String sql = """
            SELECT pu.id, pu.item_batch_id, pu.item_id, pu.vendor_id, pu.batch_quantity, pu.rate, pu.total_amount,
                   p.product_name, v.name as vendor_name
            FROM purchases pu
            LEFT JOIN products p ON pu.item_id = p.id
            LEFT JOIN vendors v ON pu.vendor_id = v.id
            ORDER BY pu.id DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PurchaseWithDetails p = new PurchaseWithDetails();
                p.setId(rs.getInt("id"));
                p.setItemBatchId(rs.getInt("item_batch_id"));
                p.setItemId(rs.getInt("item_id"));
                p.setVendorId(rs.getInt("vendor_id"));
                p.setBatchQuantity(rs.getDouble("batch_quantity"));
                p.setRate(rs.getDouble("rate"));
                p.setTotalAmount(rs.getDouble("total_amount"));
                p.setProductName(rs.getString("product_name"));
                p.setVendorName(rs.getString("vendor_name"));
                list.add(p);
            }
        }
        return list;
    }
    
    public List<PurchaseWithDetails> findAllWithDetailsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<PurchaseWithDetails> list = new ArrayList<>();
        String sql = """
            SELECT pu.id, pu.item_batch_id, pu.item_id, pu.vendor_id, pu.batch_quantity, pu.rate, pu.total_amount,
                   p.product_name, v.name as vendor_name, pu.created_at
            FROM purchases pu
            LEFT JOIN products p ON pu.item_id = p.id
            LEFT JOIN vendors v ON pu.vendor_id = v.id
            WHERE pu.created_at BETWEEN ? AND ?
            ORDER BY pu.id DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.atStartOfDay().toString());
            pstmt.setString(2, endDate.plusDays(1).atStartOfDay().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PurchaseWithDetails p = new PurchaseWithDetails();
                p.setId(rs.getInt("id"));
                p.setItemBatchId(rs.getInt("item_batch_id"));
                p.setItemId(rs.getInt("item_id"));
                p.setVendorId(rs.getInt("vendor_id"));
                p.setBatchQuantity(rs.getDouble("batch_quantity"));
                p.setRate(rs.getDouble("rate"));
                p.setTotalAmount(rs.getDouble("total_amount"));
                p.setProductName(rs.getString("product_name"));
                p.setVendorName(rs.getString("vendor_name"));
                list.add(p);
            }
        }
        return list;
    }

    public void update(Purchase purchase) throws SQLException {
        String sql = "UPDATE purchases SET batch_quantity = ?, rate = ?, total_amount = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, purchase.getBatchQuantity());
            pstmt.setDouble(2, purchase.getRate());
            pstmt.setDouble(3, purchase.getTotalAmount());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, purchase.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM purchases WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Integer getNextBatchId(int itemId, int currentBatchId) throws SQLException {
        String sql = "SELECT MIN(item_batch_id) FROM purchases WHERE item_id = ? AND item_batch_id > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, currentBatchId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    public double getBatchStock(int itemId, int batchId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(p.batch_quantity), 0) - COALESCE((
                SELECT SUM(si.quantity)
                FROM sale_items si
                WHERE si.item_id = p.item_id
                AND si.batch_id = p.item_batch_id
            ), 0) AS available_stock
            FROM purchases p
            WHERE p.item_id = ? AND p.item_batch_id = ?""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, batchId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double stock = rs.getDouble(1);
                return rs.wasNull() ? 0 : stock;
            }
        }
        return 0;
    }
    
    public double getTotalAvailableStock(int itemId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(p.batch_quantity), 0) - COALESCE((
                SELECT SUM(si.quantity)
                FROM sale_items si
                WHERE si.item_id = p.item_id
            ), 0) AS available_stock
            FROM purchases p
            WHERE p.item_id = ?""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double stock = rs.getDouble(1);
                return rs.wasNull() ? 0 : stock;
            }
        }
        return 0;
    }

    public List<Purchase> findByItemId(int itemId) throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE item_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                purchases.add(mapResultSetToPurchase(rs));
            }
        }
        return purchases;
    }

    private Purchase mapResultSetToPurchase(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setItemBatchId(rs.getInt("item_batch_id"));
        p.setItemId(rs.getInt("item_id"));
        p.setVendorId(rs.getInt("vendor_id"));
        p.setBatchQuantity(rs.getDouble("batch_quantity"));
        p.setRate(rs.getDouble("rate"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setCreatedAt(rs.getString("created_at"));
        p.setLastModifiedAt(rs.getString("last_modified_at"));
        return p;
    }

    public static class ProductWithVendor {
        private int id;
        private String productName;
        private String unit;
        private int vendorId;
        private String vendorName;
        private int currentBatchId;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public int getVendorId() { return vendorId; }
        public void setVendorId(int vendorId) { this.vendorId = vendorId; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public int getCurrentBatchId() { return currentBatchId; }
        public void setCurrentBatchId(int currentBatchId) { this.currentBatchId = currentBatchId; }

        @Override
        public String toString() {
            return productName + " (" + unit + ")";
        }
    }

    public static class PurchaseWithDetails {
        private int id;
        private int itemBatchId;
        private int itemId;
        private int vendorId;
        private double batchQuantity;
        private double rate;
        private double totalAmount;
        private String productName;
        private String vendorName;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getItemBatchId() { return itemBatchId; }
        public void setItemBatchId(int itemBatchId) { this.itemBatchId = itemBatchId; }
        public int getItemId() { return itemId; }
        public void setItemId(int itemId) { this.itemId = itemId; }
        public int getVendorId() { return vendorId; }
        public void setVendorId(int vendorId) { this.vendorId = vendorId; }
        public double getBatchQuantity() { return batchQuantity; }
        public void setBatchQuantity(double batchQuantity) { this.batchQuantity = batchQuantity; }
        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    }
}