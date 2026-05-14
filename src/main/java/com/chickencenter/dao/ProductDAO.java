package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Product;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public int create(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_name, unit, vendor_id, parent_product_id, current_batch_id, stock, price, product_source, bulk_threshold, bulk_price, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getUnit());
            pstmt.setInt(3, product.getVendorId());
            if (product.getParentProductId() != null) {
                pstmt.setInt(4, product.getParentProductId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setInt(5, product.getCurrentBatchId());
            pstmt.setDouble(6, product.getStock());
            pstmt.setDouble(7, product.getPrice());
            pstmt.setString(8, product.getProductSource() != null ? product.getProductSource() : "PURCHASE");
            pstmt.setDouble(9, product.getBulkThreshold());
            pstmt.setDouble(10, product.getBulkPrice());
            pstmt.setString(11, product.getCreatedAt().toString());
            pstmt.setString(12, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ? AND COALESCE(is_active, 1) = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        }
        return null;
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE COALESCE(is_active, 1) = 1 ORDER BY parent_product_id IS NOT NULL, product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findAllParents() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE COALESCE(is_active, 1) = 1 AND parent_product_id IS NULL ORDER BY product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public boolean hasChildren(int parentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE parent_product_id = ? AND COALESCE(is_active, 1) = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<Product> findByVendorId(int vendorId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE vendor_id = ? ORDER BY product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, unit = ?, vendor_id = ?, parent_product_id = ?, current_batch_id = ?, stock = ?, price = ?, product_source = ?, bulk_threshold = ?, bulk_price = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getUnit());
            pstmt.setInt(3, product.getVendorId());
            if (product.getParentProductId() != null) {
                pstmt.setInt(4, product.getParentProductId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setInt(5, product.getCurrentBatchId());
            pstmt.setDouble(6, product.getStock());
            pstmt.setDouble(7, product.getPrice());
            pstmt.setString(8, product.getProductSource() != null ? product.getProductSource() : "PURCHASE");
            pstmt.setDouble(9, product.getBulkThreshold());
            pstmt.setDouble(10, product.getBulkPrice());
            pstmt.setString(11, LocalDateTime.now().toString());
            pstmt.setInt(12, product.getId());
            pstmt.executeUpdate();
        }
    }

    public boolean batchExists(int itemId, int batchId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM purchases WHERE item_id = ? AND item_batch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, batchId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void updateBatch(int productId, int newBatchId) throws SQLException {
        String sql = "UPDATE products SET current_batch_id = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newBatchId);
            pstmt.setString(2, java.time.LocalDateTime.now().toString());
            pstmt.setInt(3, productId);
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Check if product exists in sale_items
        String checkSql = "SELECT COUNT(*) FROM sale_items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete product because billing history exists in sale_items");
            }
        }
        
        // Check if product exists in purchases
        checkSql = "SELECT COUNT(*) FROM purchases WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete product because purchase history exists");
            }
        }
        
        // Soft delete - set is_active = 0
        String sql = "UPDATE products SET is_active = 0, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setProductName(rs.getString("product_name"));
        product.setUnit(rs.getString("unit"));
        product.setVendorId(rs.getInt("vendor_id"));
        product.setCurrentBatchId(rs.getInt("current_batch_id"));
        int parentId = rs.getInt("parent_product_id");
        if (!rs.wasNull()) product.setParentProductId(parentId);
        product.setStock(rs.getDouble("stock"));
        product.setPrice(rs.getDouble("price"));
        product.setBulkThreshold(rs.getDouble("bulk_threshold"));
        product.setBulkPrice(rs.getDouble("bulk_price"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) product.setCreatedAt(LocalDate.parse(createdAt));
        String lastModifiedAt = rs.getString("last_modified_at");
        if (lastModifiedAt != null) product.setLastModifiedAt(LocalDateTime.parse(lastModifiedAt));
        String productSource = rs.getString("product_source");
        if (productSource != null) product.setProductSource(productSource);
        return product;
    }
}