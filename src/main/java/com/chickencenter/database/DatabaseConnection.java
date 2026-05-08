package com.chickencenter.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:chicken_center.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        setPragmas(conn);
        return conn;
    }

    private static void setPragmas(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("PRAGMA journal_mode=WAL;");
            } catch (SQLException e) {
            }
            try {
                stmt.execute("PRAGMA busy_timeout=5000;");
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
        }
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
    }
}