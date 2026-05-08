package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.ShopExpense;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShopExpenseDAO {

    public int create(ShopExpense expense) throws SQLException {
        String sql = "INSERT INTO shop_expenses (note, amount, expense_date, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, expense.getNote());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getExpenseDate().toString());
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

    public ShopExpense findById(int id) throws SQLException {
        String sql = "SELECT * FROM shop_expenses WHERE id = ?";
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

    public List<ShopExpense> findAll() throws SQLException {
        List<ShopExpense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM shop_expenses ORDER BY expense_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    public List<ShopExpense> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<ShopExpense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM shop_expenses WHERE expense_date BETWEEN ? AND ? ORDER BY expense_date DESC";
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

    public void update(ShopExpense expense) throws SQLException {
        String sql = "UPDATE shop_expenses SET note = ?, amount = ?, expense_date = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, expense.getNote());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getExpenseDate().toString());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, expense.getId());
            pstmt.executeUpdate();
        }
    }

    public double getTotalExpenses() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM shop_expenses";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    private ShopExpense mapResultSetToExpense(ResultSet rs) throws SQLException {
        ShopExpense expense = new ShopExpense();
        expense.setId(rs.getInt("id"));
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