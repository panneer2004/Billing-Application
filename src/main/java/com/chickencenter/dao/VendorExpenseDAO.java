package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.VendorExpense;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendorExpenseDAO {

    public int create(VendorExpense expense) throws SQLException {
        String sql = "INSERT INTO vendor_expenses (vendor_id, note, amount, expense_date, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, expense.getVendorId());
            pstmt.setString(2, expense.getNote());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setString(4, expense.getExpenseDate().toString());
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

    public VendorExpense findById(int id) throws SQLException {
        String sql = "SELECT * FROM vendor_expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToExpense(rs);
            }
        }
        return null;
    }

    public List<VendorExpense> findAll() throws SQLException {
        List<VendorExpense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM vendor_expenses ORDER BY expense_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    public List<VendorExpense> findByVendorId(int vendorId) throws SQLException {
        List<VendorExpense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM vendor_expenses WHERE vendor_id = ? ORDER BY expense_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    public List<VendorExpense> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<VendorExpense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM vendor_expenses WHERE expense_date BETWEEN ? AND ? ORDER BY expense_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    public void update(VendorExpense expense) throws SQLException {
        String sql = "UPDATE vendor_expenses SET vendor_id = ?, note = ?, amount = ?, expense_date = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, expense.getVendorId());
            pstmt.setString(2, expense.getNote());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setString(4, expense.getExpenseDate().toString());
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setInt(6, expense.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vendor_expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public double getTotalExpenses() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM vendor_expenses";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getTotalExpensesByVendor(int vendorId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM vendor_expenses WHERE vendor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    private VendorExpense mapResultSetToExpense(ResultSet rs) throws SQLException {
        VendorExpense expense = new VendorExpense();
        expense.setId(rs.getInt("id"));
        expense.setVendorId(rs.getInt("vendor_id"));
        expense.setNote(rs.getString("note"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setExpenseDate(LocalDate.parse(rs.getString("expense_date")));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) expense.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) expense.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return expense;
    }
}
