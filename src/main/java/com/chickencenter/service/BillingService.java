package com.chickencenter.service;

import com.chickencenter.dao.PriceDAO;
import com.chickencenter.dao.PurchaseDAO;
import com.chickencenter.dao.ProductDAO;
import com.chickencenter.dao.SaleDAO;
import com.chickencenter.dao.SaleItemDAO;
import com.chickencenter.dao.StockDAO;
import com.chickencenter.model.Price;
import com.chickencenter.model.Product;
import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BillingService {
    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;
    private final PriceDAO priceDAO;
    private final StockDAO stockDAO;
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;

    public BillingService() {
        this.saleDAO = new SaleDAO();
        this.saleItemDAO = new SaleItemDAO();
        this.priceDAO = new PriceDAO();
        this.stockDAO = new StockDAO();
        this.productDAO = new ProductDAO();
        this.purchaseDAO = new PurchaseDAO();
    }

    public Sale createSale(LocalDate saleDate) throws SQLException {
        Sale sale = new Sale(saleDate);
        int saleId = saleDAO.create(sale);
        sale.setId(saleId);
        return sale;
    }

    public int addItemToCart(SaleItem saleItem) throws SQLException {
        double availableStock = getAvailableStock(saleItem.getItemId());
        if (availableStock < saleItem.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + availableStock);
        }

        int itemId = saleItemDAO.create(saleItem);
        saleItem.setId(itemId);
        recalculateSaleTotal(saleItem.getSaleId());
        return itemId;
    }

    public void removeItemFromSale(int saleItemId) throws SQLException {
        SaleItem saleItem = saleItemDAO.findById(saleItemId);
        if (saleItem != null) {
            Product product = productDAO.findById(saleItem.getItemId());
            if (product != null && "STOCK".equalsIgnoreCase(product.getProductSource())) {
                stockDAO.addQuantity(saleItem.getItemId(), saleItem.getQuantity());
            }
            saleItemDAO.delete(saleItemId);
            recalculateSaleTotal(saleItem.getSaleId());
        }
    }

    private void recalculateSaleTotal(int saleId) throws SQLException {
        List<SaleItem> items = saleItemDAO.findBySaleId(saleId);
        double total = items.stream().mapToDouble(SaleItem::getTotal).sum();
        saleDAO.updateTotalAmount(saleId, total);
    }

    public void completeSaleWithPayment(int saleId, boolean isBilled, String paymentMode, double cashAmount, double gpayAmount) throws SQLException {
        saleDAO.markAsBilled(saleId, isBilled);
        saleDAO.updatePaymentInfo(saleId, paymentMode, cashAmount, gpayAmount);
        
        List<SaleItem> items = saleItemDAO.findBySaleId(saleId);
        for (SaleItem item : items) {
            Product product = productDAO.findById(item.getItemId());
            if (product != null && "STOCK".equalsIgnoreCase(product.getProductSource())) {
                stockDAO.reduceQuantity(item.getItemId(), item.getQuantity());
            }
        }
    }

    public void deleteSale(int saleId) throws SQLException {
        List<SaleItem> items = saleItemDAO.findBySaleId(saleId);
        for (SaleItem item : items) {
            Product product = productDAO.findById(item.getItemId());
            if (product != null && "STOCK".equalsIgnoreCase(product.getProductSource())) {
                stockDAO.addQuantity(item.getItemId(), item.getQuantity());
            }
        }
        saleItemDAO.deleteBySaleId(saleId);
        saleDAO.delete(saleId);
    }

    public List<SaleItem> getSaleItems(int saleId) throws SQLException {
        return saleItemDAO.findBySaleId(saleId);
    }

    public List<Sale> getAllSales() throws SQLException {
        return saleDAO.findAll();
    }

    public List<Sale> getFilteredSales(LocalDate fromDate, LocalDate toDate) throws SQLException {
        return saleDAO.findByDateRange(fromDate, toDate);
    }

    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.findByDateRange(startDate, endDate);
    }

    public double getCurrentPrice(int itemId) throws SQLException {
        Price price = priceDAO.findLatestByItemId(itemId);
        return price != null ? price.getPrice() : 0;
    }

    public double getProductPrice(int itemId) throws SQLException {
        Product product = productDAO.findById(itemId);
        return product != null ? product.getPrice() : 0;
    }

    public double getAvailableStock(int itemId) throws SQLException {
        Product product = productDAO.findById(itemId);
        if (product == null) return 0;

        String source = product.getProductSource();
        if (source != null && source.equalsIgnoreCase("STOCK")) {
            var stock = stockDAO.findByItemId(itemId);
            return stock != null ? stock.getQuantity() : 0;
        }

        double totalPurchased = purchaseDAO.getTotalAvailableStock(itemId);
        double totalSold = saleItemDAO.getTotalSoldQuantity(itemId);
        return totalPurchased - totalSold;
    }

    public double getTotalCashByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.getTotalCashByDateRange(startDate, endDate);
    }

    public double getTotalGPayByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.getTotalGPayByDateRange(startDate, endDate);
    }

}
