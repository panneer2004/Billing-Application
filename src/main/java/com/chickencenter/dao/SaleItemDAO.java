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

    public double getTotalSoldQuantityIncludingChildren(int parentId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(si.quantity), 0)
            FROM sale_items si
            JOIN products p ON si.item_id = p.id
            WHERE p.id = ? OR p.parent_product_id = ?""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            pstmt.setInt(2, parentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    public double getTotalSoldQuantityForBatchIncludingChildren(int parentId, int batchId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(si.quantity), 0)
            FROM sale_items si
            JOIN products p ON si.item_id = p.id
            WHERE (p.id = ? OR p.parent_product_id = ?) AND si.batch_id = ?""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            pstmt.setInt(2, parentId);
            pstmt.setInt(3, batchId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
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

    public List<Object[]> getItemSales(LocalDate fromDate, LocalDate toDate, Integer productId, Integer batchId) throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT s.id, p.product_name, " +
                     "COALESCE(sbc.batch_id, si.batch_id) AS batch_id, " +
                     "COALESCE(sbc.consumed_quantity, si.quantity) AS qty, " +
                     "si.actualprice AS unit_price, " +
                     "CASE WHEN sbc.consumed_quantity IS NOT NULL " +
                     "     THEN ROUND(si.discount_amount * sbc.consumed_quantity / NULLIF(si.quantity, 0), 2) " +
                     "     ELSE si.discount_amount END AS discount, " +
                     "COALESCE(sbc.consumed_quantity * si.actualprice, si.total) AS amount " +
                     "FROM sale_items si " +
                     "JOIN sales s ON s.id = si.sale_id " +
                     "JOIN products p ON p.id = si.item_id " +
                     "LEFT JOIN sale_batch_consumption sbc ON sbc.sale_item_id = si.id " +
                     "WHERE DATE(s.created_at) BETWEEN ? AND ? " +
                     "AND (? IS NULL OR si.item_id = ?) " +
                     "AND (? IS NULL OR COALESCE(sbc.batch_id, si.batch_id) = ?) " +
                     "ORDER BY s.id DESC, si.id, sbc.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            if (productId != null) {
                pstmt.setInt(3, productId);
                pstmt.setInt(4, productId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
                pstmt.setNull(4, Types.INTEGER);
            }
            if (batchId != null) {
                pstmt.setInt(5, batchId);
                pstmt.setInt(6, batchId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
                pstmt.setNull(6, Types.INTEGER);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getInt(3),
                    rs.getDouble(4),
                    rs.getDouble(5),
                    rs.getDouble(6),
                    rs.getDouble(7)
                });
            }
        }
        return result;
    }

    public List<Integer> getDistinctBatches(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Integer> batches = new ArrayList<>();
        String sql = "SELECT DISTINCT COALESCE(sbc.batch_id, si.batch_id) AS batch_id " +
                     "FROM sale_items si " +
                     "JOIN sales s ON s.id = si.sale_id " +
                     "LEFT JOIN sale_batch_consumption sbc ON sbc.sale_item_id = si.id " +
                     "WHERE DATE(s.created_at) BETWEEN ? AND ? " +
                     "AND COALESCE(sbc.batch_id, si.batch_id) IS NOT NULL " +
                     "ORDER BY batch_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(rs.getInt(1));
            }
        }
        return batches;
    }

    public List<Integer> getDistinctBatchesForProduct(int productId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Integer> batches = new ArrayList<>();
        String sql = "SELECT DISTINCT COALESCE(sbc.batch_id, si.batch_id) AS batch_id " +
                     "FROM sale_items si " +
                     "JOIN sales s ON s.id = si.sale_id " +
                     "LEFT JOIN sale_batch_consumption sbc ON sbc.sale_item_id = si.id " +
                     "WHERE si.item_id = ? AND DATE(s.created_at) BETWEEN ? AND ? " +
                     "AND COALESCE(sbc.batch_id, si.batch_id) IS NOT NULL " +
                     "ORDER BY batch_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, fromDate.toString());
            pstmt.setString(3, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                batches.add(rs.getInt(1));
            }
        }
        return batches;
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
