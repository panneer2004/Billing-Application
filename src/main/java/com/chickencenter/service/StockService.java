package com.chickencenter.service;

import com.chickencenter.dao.StockDAO;
import com.chickencenter.model.Stock;

import java.sql.SQLException;
import java.util.List;

public class StockService {
    private final StockDAO stockDAO;

    public StockService() {
        this.stockDAO = new StockDAO();
    }

    public List<Stock> getAllStock() throws SQLException {
        return stockDAO.findAll();
    }

    public void addStock(int productId, double quantity) throws SQLException {
        Stock existingStock = stockDAO.findByItemId(productId);
        if (existingStock != null) {
            double newQuantity = existingStock.getQuantity() + quantity;
            stockDAO.updateQuantity(productId, newQuantity);
        } else {
            Stock newStock = new Stock(productId, quantity);
            stockDAO.create(newStock);
        }
    }

    public void updateStock(int productId, double quantity) throws SQLException {
        stockDAO.updateQuantity(productId, quantity);
    }
}
