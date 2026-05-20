package com.chickencenter.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    contact_number TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    vendor_id INTEGER,
                    unit TEXT,
                    category TEXT,
                    current_batch_id INTEGER,
                    is_active INTEGER DEFAULT 1,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )
            """);

            try { stmt.execute("ALTER TABLE items ADD COLUMN category TEXT"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE items ADD COLUMN is_active INTEGER DEFAULT 1"); } catch (SQLException e) {}

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS purchase_batches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_batch_id INTEGER,
                    item_id INTEGER NOT NULL,
                    vendor_id INTEGER,
                    batch_quantity REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_id) REFERENCES items(id),
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS price_list (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    price_date TEXT NOT NULL,
                    price REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_id) REFERENCES items(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    total_amount REAL DEFAULT 0,
                    is_billed INTEGER DEFAULT 0,
                    sale_date TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    batch_id INTEGER,
                    quantity REAL NOT NULL,
                    price REAL NOT NULL,
                    actualprice REAL DEFAULT 0,
                    total REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sale_id) REFERENCES sales(id),
                    FOREIGN KEY (item_id) REFERENCES items(id),
                    FOREIGN KEY (batch_id) REFERENCES purchase_batches(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendor_expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vendor_id INTEGER NOT NULL,
                    note TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    gender TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS employee_expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id INTEGER NOT NULL,
                    note TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (employee_id) REFERENCES employees(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shop_expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    note TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_items_vendor ON items(vendor_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_purchase_batches_item ON purchase_batches(item_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_price_list_item ON price_list(item_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items(sale_id)");
            addColumnIfNotExists(stmt, "sale_items", "actualprice", "REAL DEFAULT 0");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date)");

            addColumnIfNotExists(stmt, "sales", "payment_mode", "TEXT DEFAULT 'Cash'");
            addColumnIfNotExists(stmt, "sales", "cash_amount", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "sales", "gpay_amount", "REAL DEFAULT 0");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_name TEXT NOT NULL,
                    unit TEXT NOT NULL,
                    vendor_id INTEGER,
                    parent_product_id INTEGER,
                    current_batch_id INTEGER DEFAULT 0,
                    stock REAL DEFAULT 0,
                    price REAL DEFAULT 0,
                    is_active INTEGER DEFAULT 1,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
                    FOREIGN KEY (parent_product_id) REFERENCES products(id)
                )
            """);

            addColumnIfNotExists(stmt, "products", "current_batch_id", "INTEGER DEFAULT 0");
            addColumnIfNotExists(stmt, "products", "stock", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "products", "price", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "products", "is_active", "INTEGER DEFAULT 1");
            addColumnIfNotExists(stmt, "products", "product_source", "TEXT DEFAULT 'PURCHASE'");
            addColumnIfNotExists(stmt, "products", "bulk_threshold", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "products", "bulk_price", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "products", "parent_product_id", "INTEGER");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_batch_id INTEGER,
                    item_id INTEGER NOT NULL,
                    vendor_id INTEGER NOT NULL,
                    batch_quantity REAL NOT NULL,
                    rate REAL DEFAULT 0,
                    total_amount REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_id) REFERENCES products(id),
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS account (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    shop_name TEXT DEFAULT 'My Shop',
                    shop_address TEXT,
                    contact_no1 TEXT,
                    contact_no2 TEXT,
                    contact_no3 TEXT,
                    password TEXT,
                    is_locked INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    last_modified_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            try { stmt.execute("ALTER TABLE account ADD COLUMN shop_address TEXT"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE account ADD COLUMN contact_no1 TEXT"); } catch (SQLException e) {}
            addColumnIfNotExists(stmt, "account", "contact_no2", "TEXT");
            addColumnIfNotExists(stmt, "account", "contact_no3", "TEXT");
            addColumnIfNotExists(stmt, "account", "password", "TEXT");
            addColumnIfNotExists(stmt, "account", "is_locked", "INTEGER DEFAULT 0");
            addColumnIfNotExists(stmt, "account", "printer_name", "TEXT");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sale_batch_consumption (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    sale_item_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    batch_id INTEGER NOT NULL,
                    consumed_quantity REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sale_id) REFERENCES sales(id),
                    FOREIGN KEY (sale_item_id) REFERENCES sale_items(id)
                )
            """);

            addColumnIfNotExists(stmt, "purchases", "rate", "REAL DEFAULT 0");
            boolean balanceColumnAdded = addColumnIfNotExists(stmt, "purchases", "balance_quantity", "REAL DEFAULT 0");
            addColumnIfNotExists(stmt, "sale_items", "discount_amount", "REAL DEFAULT 0");

            if (balanceColumnAdded) {
                stmt.execute("""
                    UPDATE purchases SET balance_quantity = batch_quantity - COALESCE((
                        SELECT SUM(si.quantity) FROM sale_items si
                        WHERE si.item_id = purchases.item_id AND si.batch_id = purchases.item_batch_id
                    ), 0) WHERE COALESCE(balance_quantity, 0) = 0 AND batch_quantity > 0
                """);
                System.out.println("Migrated existing purchase records with balance_quantity.");
            }

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM account");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO account (shop_name, shop_address, contact_no1) VALUES ('JK CHICKEN CENTER', 'Address Here', '1234567890')");
            }

            System.out.println("Database initialized successfully!");
            
            restoreMissingProducts(stmt);
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static boolean addColumnIfNotExists(Statement stmt, String tableName, String columnName, String definition) throws SQLException {
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
        boolean columnExists = false;
        while (rs.next()) {
            if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                columnExists = true;
                break;
            }
        }
        if (!columnExists) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
            return true;
        }
        return false;
    }

    private static void restoreMissingProducts(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery(
            "SELECT DISTINCT si.item_id FROM sale_items si " +
            "LEFT JOIN items i ON si.item_id = i.id " +
            "WHERE i.id IS NULL"
        );
        
        int count = 0;
        while (rs.next()) {
            int itemId = rs.getInt("item_id");
            try {
                stmt.execute("INSERT OR IGNORE INTO items (id, name, vendor_id, unit, category, current_batch_id) " +
                           "VALUES (" + itemId + ", 'Product #" + itemId + "', NULL, 'pcs', 'General', 0)");
                count++;
            } catch (SQLException e) {
                // Skip if insert fails
            }
        }
        
        if (count > 0) {
            System.out.println("Restored " + count + " missing product records to items table.");
        }
    }
}