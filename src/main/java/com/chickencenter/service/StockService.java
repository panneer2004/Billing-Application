package com.chickencenter.service;

import com.chickencenter.dao.ItemDAO;
import com.chickencenter.dao.PriceDAO;
import com.chickencenter.dao.PurchaseBatchDAO;
import com.chickencenter.dao.StockDAO;
import com.chickencenter.model.Item;
import com.chickencenter.model.Price;
import com.chickencenter.model.PurchaseBatch;
import com.chickencenter.model.Stock;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StockService {
    private final StockDAO stockDAO;
    private final ItemDAO itemDAO;
    private final PurchaseBatchDAO purchaseBatchDAO;
    private final PriceDAO priceDAO;

    public StockService() {
        this.stockDAO = new StockDAO();
        this.itemDAO = new ItemDAO();
        this.purchaseBatchDAO = new PurchaseBatchDAO();
        this.priceDAO = new PriceDAO();
    }

    public void purchaseStock(int itemId, int vendorId, double quantity, double totalAmount, double purchasePricePerUnit) throws SQLException {
        PurchaseBatch batch = new PurchaseBatch(itemId, vendorId, quantity, totalAmount);
        int batchId = purchaseBatchDAO.create(batch);
        
        batch.setItemBatchId(batchId);
        purchaseBatchDAO.update(batch);

        Item item = itemDAO.findById(itemId);
        if (item != null) {
            item.setCurrentBatchId(batchId);
            itemDAO.update(item);
        }

        stockDAO.addQuantity(itemId, quantity);
        priceDAO.setPriceForItem(itemId, purchasePricePerUnit);
    }

    public void reduceStock(int itemId, double quantity) throws SQLException {
        stockDAO.reduceQuantity(itemId, quantity);
    }

    public double getStockQuantity(int itemId) throws SQLException {
        var stock = stockDAO.findByItemId(itemId);
        return stock != null ? stock.getQuantity() : 0;
    }

    public List<Item> getAllItemsWithStock() throws SQLException {
        return itemDAO.findAll();
    }

    public List<Item> getLowStockItems(double threshold) throws SQLException {
        List<Item> allItems = itemDAO.findAll();
        return allItems.stream()
                .filter(item -> {
                    try {
                        double qty = getStockQuantity(item.getId());
                        return qty <= threshold;
                    } catch (SQLException e) {
                        return false;
                    }
                })
                .toList();
    }

    public void updateStockManually(int itemId, double newQuantity) throws SQLException {
        stockDAO.updateQuantity(itemId, newQuantity);
    }

    public List<PurchaseBatch> getPurchaseHistory(int itemId) throws SQLException {
        return purchaseBatchDAO.findByItemId(itemId);
    }

    public double getCurrentPurchasePrice(int itemId) throws SQLException {
        Price latestPrice = priceDAO.findLatestByItemId(itemId);
        return latestPrice != null ? latestPrice.getPrice() : 0;
    }

    public List<PurchaseBatch> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return purchaseBatchDAO.findByDateRange(startDate, endDate);
    }

    public double getTotalPurchasesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<PurchaseBatch> purchases = getPurchasesByDateRange(startDate, endDate);
        return purchases.stream().mapToDouble(PurchaseBatch::getTotalAmount).sum();
    }
    
    public Stock getStockByItemId(int itemId) throws SQLException {
        return stockDAO.findByItemId(itemId);
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
    
    public void deleteStock(int productId) throws SQLException {
        stockDAO.deleteByItemId(productId);
    }
}
