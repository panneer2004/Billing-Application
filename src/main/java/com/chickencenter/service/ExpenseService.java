package com.chickencenter.service;

import com.chickencenter.dao.EmployeeDAO;
import com.chickencenter.dao.EmployeeExpenseDAO;
import com.chickencenter.dao.PurchaseBatchDAO;
import com.chickencenter.dao.SaleDAO;
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
    private final SaleDAO saleDAO;
    private final PurchaseBatchDAO purchaseBatchDAO;

    public ExpenseService() {
        this.vendorDAO = new VendorDAO();
        this.vendorExpenseDAO = new VendorExpenseDAO();
        this.employeeDAO = new EmployeeDAO();
        this.employeeExpenseDAO = new EmployeeExpenseDAO();
        this.shopExpenseDAO = new ShopExpenseDAO();
        this.saleDAO = new SaleDAO();
        this.purchaseBatchDAO = new PurchaseBatchDAO();
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

    public void deleteVendor(int id) throws SQLException {
        vendorDAO.delete(id);
    }

    public int createVendorExpense(VendorExpense expense) throws SQLException {
        return vendorExpenseDAO.create(expense);
    }

    public List<VendorExpense> getVendorExpenses(int vendorId) throws SQLException {
        return vendorExpenseDAO.findByVendorId(vendorId);
    }

    public List<VendorExpense> getAllVendorExpenses() throws SQLException {
        return vendorExpenseDAO.findAll();
    }

    public List<VendorExpense> getVendorExpensesByDateRange(LocalDate start, LocalDate end) throws SQLException {
        return vendorExpenseDAO.findByDateRange(start, end);
    }

    public void updateVendorExpense(VendorExpense expense) throws SQLException {
        vendorExpenseDAO.update(expense);
    }

    public void deleteVendorExpense(int id) throws SQLException {
        vendorExpenseDAO.delete(id);
    }

    public double getTotalVendorExpenses() throws SQLException {
        return vendorExpenseDAO.getTotalExpenses();
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

    public void deleteEmployee(int id) throws SQLException {
        employeeDAO.delete(id);
    }

    public int createEmployeeExpense(EmployeeExpense expense) throws SQLException {
        return employeeExpenseDAO.create(expense);
    }

    public List<EmployeeExpense> getEmployeeExpenses(int employeeId) throws SQLException {
        return employeeExpenseDAO.findByEmployeeId(employeeId);
    }

    public List<EmployeeExpense> getAllEmployeeExpenses() throws SQLException {
        return employeeExpenseDAO.findAll();
    }

    public List<EmployeeExpense> getEmployeeExpensesByDateRange(LocalDate start, LocalDate end) throws SQLException {
        return employeeExpenseDAO.findByDateRange(start, end);
    }

    public void updateEmployeeExpense(EmployeeExpense expense) throws SQLException {
        employeeExpenseDAO.update(expense);
    }

    public void deleteEmployeeExpense(int id) throws SQLException {
        employeeExpenseDAO.delete(id);
    }

    public double getTotalEmployeeExpenses() throws SQLException {
        return employeeExpenseDAO.getTotalExpenses();
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

    public double getTotalShopExpenses() throws SQLException {
        return shopExpenseDAO.getTotalExpenses();
    }

    public double getTotalExpenses() throws SQLException {
        return getTotalVendorExpenses() + getTotalEmployeeExpenses() + getTotalShopExpenses();
    }

    public double getTotalPurchases() throws SQLException {
        return purchaseBatchDAO.getTotalPurchaseAmount();
    }

    public double getTotalSales() throws SQLException {
        return saleDAO.getTotalSalesAmount();
    }

    public double getProfitLoss() throws SQLException {
        return getTotalSales() - getTotalPurchases();
    }

    public double getNetProfit() throws SQLException {
        return getTotalSales() - getTotalPurchases() - getTotalExpenses();
    }
}
