package com.chickencenter.service;

import com.chickencenter.dao.EmployeeDAO;
import com.chickencenter.dao.EmployeeExpenseDAO;
import com.chickencenter.dao.ShopExpenseDAO;
import com.chickencenter.dao.VendorDAO;
import com.chickencenter.dao.VendorExpenseDAO;
import com.chickencenter.model.Employee;
import com.chickencenter.model.EmployeeExpense;
import com.chickencenter.model.ShopExpense;
import com.chickencenter.model.Vendor;
import com.chickencenter.model.VendorExpense;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ExpenseService {
    private final VendorDAO vendorDAO;
    private final VendorExpenseDAO vendorExpenseDAO;
    private final EmployeeDAO employeeDAO;
    private final EmployeeExpenseDAO employeeExpenseDAO;
    private final ShopExpenseDAO shopExpenseDAO;

    public ExpenseService() {
        this.vendorDAO = new VendorDAO();
        this.vendorExpenseDAO = new VendorExpenseDAO();
        this.employeeDAO = new EmployeeDAO();
        this.employeeExpenseDAO = new EmployeeExpenseDAO();
        this.shopExpenseDAO = new ShopExpenseDAO();
    }

    public int createVendor(Vendor vendor) throws SQLException {
        return vendorDAO.create(vendor);
    }

    public Vendor getVendor(int id) throws SQLException {
        return vendorDAO.findById(id);
    }

    public List<Vendor> getAllVendors() throws SQLException {
        return vendorDAO.findAll();
    }

    public void updateVendor(Vendor vendor) throws SQLException {
        vendorDAO.update(vendor);
    }

    public int createVendorExpense(VendorExpense expense) throws SQLException {
        return vendorExpenseDAO.create(expense);
    }

    public List<VendorExpense> getAllVendorExpenses() throws SQLException {
        return vendorExpenseDAO.findAll();
    }

    public List<VendorExpense> getVendorExpensesByDateRange(LocalDate start, LocalDate end) throws SQLException {
        return vendorExpenseDAO.findByDateRange(start, end);
    }

    public void deleteVendorExpense(int id) throws SQLException {
        vendorExpenseDAO.delete(id);
    }

    public int createEmployee(Employee employee) throws SQLException {
        return employeeDAO.create(employee);
    }

    public Employee getEmployee(int id) throws SQLException {
        return employeeDAO.findById(id);
    }

    public List<Employee> getAllEmployees() throws SQLException {
        return employeeDAO.findAll();
    }

    public void updateEmployee(Employee employee) throws SQLException {
        employeeDAO.update(employee);
    }

    public int createEmployeeExpense(EmployeeExpense expense) throws SQLException {
        return employeeExpenseDAO.create(expense);
    }

    public List<EmployeeExpense> getAllEmployeeExpenses() throws SQLException {
        return employeeExpenseDAO.findAll();
    }

    public List<EmployeeExpense> getEmployeeExpensesByDateRange(LocalDate start, LocalDate end) throws SQLException {
        return employeeExpenseDAO.findByDateRange(start, end);
    }

    public void deleteEmployeeExpense(int id) throws SQLException {
        employeeExpenseDAO.delete(id);
    }

    public int createShopExpense(ShopExpense expense) throws SQLException {
        return shopExpenseDAO.create(expense);
    }

    public List<ShopExpense> getAllShopExpenses() throws SQLException {
        return shopExpenseDAO.findAll();
    }

    public List<ShopExpense> getShopExpensesByDateRange(LocalDate start, LocalDate end) throws SQLException {
        return shopExpenseDAO.findByDateRange(start, end);
    }

    public void updateShopExpense(ShopExpense expense) throws SQLException {
        shopExpenseDAO.update(expense);
    }
}
