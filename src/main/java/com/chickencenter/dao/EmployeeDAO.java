package com.chickencenter.dao;

import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Employee;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public int create(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (name, gender, created_at, last_modified_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getGender());
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

    public Employee findById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        }
        return null;
    }

    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }
        return employees;
    }

    public void update(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET name = ?, gender = ?, last_modified_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getGender());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setInt(4, employee.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Employee> searchByName(String name) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }
        return employees;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setName(rs.getString("name"));
        employee.setGender(rs.getString("gender"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) employee.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastModified = rs.getString("last_modified_at");
        if (lastModified != null) employee.setLastModifiedAt(LocalDateTime.parse(lastModified));
        return employee;
    }
}
