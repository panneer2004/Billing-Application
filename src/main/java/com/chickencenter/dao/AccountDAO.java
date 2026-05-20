package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Account;
import java.sql.*;
import java.time.LocalDateTime;

public class AccountDAO {

    public Account getAccount() throws SQLException {
        String sql = "SELECT * FROM account LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("id"));
                account.setShopName(rs.getString("shop_name"));
                account.setShopAddress(rs.getString("shop_address"));
                account.setContactNo1(rs.getString("contact_no1"));
                account.setContactNo2(rs.getString("contact_no2"));
                account.setContactNo3(rs.getString("contact_no3"));
                account.setPassword(rs.getString("password"));
                account.setLocked(rs.getInt("is_locked") == 1);
                account.setPrinterName(rs.getString("printer_name"));
                account.setCreatedAt(rs.getString("created_at"));
                account.setLastModifiedAt(rs.getString("last_modified_at"));
                return account;
            }
        }
        return null;
    }

    public void updateAccount(Account account) throws SQLException {
        String sql = "UPDATE account SET shop_name = ?, shop_address = ?, contact_no1 = ?, contact_no2 = ?, contact_no3 = ?, password = ?, is_locked = ?, printer_name = ?, last_modified_at = ? WHERE id = (SELECT id FROM account LIMIT 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getShopName() != null ? account.getShopName().trim() : "");
            pstmt.setString(2, account.getShopAddress() != null ? account.getShopAddress().trim() : "");
            pstmt.setString(3, account.getContactNo1() != null ? account.getContactNo1().trim() : "");
            pstmt.setString(4, account.getContactNo2() != null ? account.getContactNo2().trim() : "");
            pstmt.setString(5, account.getContactNo3() != null ? account.getContactNo3().trim() : "");
            pstmt.setString(6, account.getPassword() != null ? account.getPassword().trim() : "");
            pstmt.setInt(7, account.isLocked() ? 1 : 0);
            pstmt.setString(8, account.getPrinterName() != null ? account.getPrinterName().trim() : "");
            pstmt.setString(9, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        }
    }
}