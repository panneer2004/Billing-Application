package com.chickencenter.service;

import com.chickencenter.dao.PriceDAO;
import com.chickencenter.dao.PurchaseDAO;
import com.chickencenter.dao.ProductDAO;
import com.chickencenter.dao.SaleDAO;
import com.chickencenter.dao.SaleItemDAO;
import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Price;
import com.chickencenter.model.Product;
import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;
import com.chickencenter.util.ToastManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillingService {
    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;
    private final PriceDAO priceDAO;
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;

    public BillingService() {
        this.saleDAO = new SaleDAO();
        this.saleItemDAO = new SaleItemDAO();
        this.priceDAO = new PriceDAO();
        this.productDAO = new ProductDAO();
        this.purchaseDAO = new PurchaseDAO();
    }

    private int getEffectiveProductId(Product product) {
        return product.getParentProductId() != null && product.getParentProductId() > 0
               ? product.getParentProductId() : product.getId();
    }

    public Sale createSale(double totalAmount, LocalDate saleDate) throws SQLException {
        Sale sale = new Sale(saleDate);
        sale.setTotalAmount(totalAmount);
        int saleId = saleDAO.create(sale);
        sale.setId(saleId);
        return sale;
    }

    public void saveSaleItems(int saleId, List<SaleItem> items) throws SQLException {
        for (SaleItem item : items) {
            item.setSaleId(saleId);
            double availableStock = getAvailableStock(item.getItemId());
            if (availableStock < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item. Available: " + availableStock);
            }
            int itemId = saleItemDAO.create(item);
            item.setId(itemId);
        }
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
                int effectiveId = getEffectiveProductId(product);
                Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                if (effectiveProduct != null) {
                    effectiveProduct.setStock(effectiveProduct.getStock() + saleItem.getQuantity());
                    productDAO.update(effectiveProduct);
                }
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
        Set<Integer> productsToCheck = new HashSet<>();
        
        for (SaleItem item : items) {
            Product product = productDAO.findById(item.getItemId());
            if (product == null) continue;

            if ("STOCK".equalsIgnoreCase(product.getProductSource())) {
                int effectiveId = getEffectiveProductId(product);
                Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                if (effectiveProduct != null) {
                    effectiveProduct.setStock(Math.max(0, effectiveProduct.getStock() - item.getQuantity()));
                    productDAO.update(effectiveProduct);
                    productsToCheck.add(effectiveId);
                }
            } else if (item.getBatchId() != null && item.getBatchId() > 0) {
                int effectiveId = getEffectiveProductId(product);
                double remainingQty = item.getQuantity();
                int currentBatch = item.getBatchId();

                while (remainingQty > 0) {
                    double batchBalance = purchaseDAO.getBalanceQuantity(effectiveId, currentBatch);
                    double toConsume = Math.min(remainingQty, batchBalance);
                    if (toConsume > 0) {
                        purchaseDAO.reduceBalanceQuantity(effectiveId, currentBatch, toConsume);
                        insertBatchConsumption(saleId, item.getId(), effectiveId, currentBatch, toConsume);
                        remainingQty -= toConsume;
                    }
                    if (remainingQty > 0) {
                        Integer nextBatch = purchaseDAO.getNextBatchWithPositiveBalance(effectiveId, currentBatch);
                        if (nextBatch == null) break;
                        currentBatch = nextBatch;
                    }
                }

                Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                if (effectiveProduct != null) {
                    double currentBalance = purchaseDAO.getBalanceQuantity(effectiveId, effectiveProduct.getCurrentBatchId());
                    effectiveProduct.setStock(Math.max(0, currentBalance));
                    productDAO.update(effectiveProduct);
                    productsToCheck.add(effectiveId);
                }
            }
        }
        
        for (int productId : productsToCheck) {
            autoSwitchBatchIfExhausted(productId);
        }
    }

    private void insertBatchConsumption(int saleId, int saleItemId, int itemId, int batchId, double quantity) throws SQLException {
        String sql = "INSERT INTO sale_batch_consumption (sale_id, sale_item_id, item_id, batch_id, consumed_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            pstmt.setInt(2, saleItemId);
            pstmt.setInt(3, itemId);
            pstmt.setInt(4, batchId);
            pstmt.setDouble(5, quantity);
            pstmt.executeUpdate();
        }
    }

    private void autoSwitchBatchIfExhausted(int productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) return;
        productId = getEffectiveProductId(product);
        product = productDAO.findById(productId);
        if (product == null) return;
        int currentBatchId = product.getCurrentBatchId();
        if (currentBatchId <= 0) return;
        double currentBalance = purchaseDAO.getBalanceQuantity(productId, currentBatchId);
        if (currentBalance > 0) return;
        Integer nextBatchId = purchaseDAO.getNextBatchWithPositiveBalance(productId, currentBatchId);
        if (nextBatchId != null) {
            double newBalance = purchaseDAO.getBalanceQuantity(productId, nextBatchId);
            product.setCurrentBatchId(nextBatchId);
            product.setStock(newBalance);
            productDAO.update(product);
            String productName = product.getProductName() != null ? product.getProductName() : "Product #" + productId;
            ToastManager.showError("⚠ Batch for " + productName + " changed to Batch " + nextBatchId);
        } else {
            product.setStock(0);
            productDAO.update(product);
        }
    }

    public void deleteSale(int saleId) throws SQLException {
        List<SaleItem> items = saleItemDAO.findBySaleId(saleId);
        List<Object[]> consumptions = getBatchConsumptions(saleId);
        Set<Integer> productsToUpdate = new HashSet<>();

        if (!consumptions.isEmpty()) {
            for (Object[] c : consumptions) {
                int itemId = (int) c[0];
                int batchId = (int) c[1];
                double quantity = (double) c[2];
                purchaseDAO.addBalanceQuantity(itemId, batchId, quantity);
                productsToUpdate.add(itemId);
            }

            for (int productId : productsToUpdate) {
                Product product = productDAO.findById(productId);
                if (product == null) continue;
                double currentBalance = purchaseDAO.getBalanceQuantity(productId, product.getCurrentBatchId());
                product.setStock(currentBalance);
                productDAO.update(product);
                if (currentBalance <= 0) {
                    autoSwitchBatchIfExhausted(productId);
                }
            }

            for (SaleItem item : items) {
                Product product = productDAO.findById(item.getItemId());
                if (product != null && "STOCK".equalsIgnoreCase(product.getProductSource())) {
                    int effectiveId = getEffectiveProductId(product);
                    Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                    if (effectiveProduct != null) {
                        effectiveProduct.setStock(effectiveProduct.getStock() + item.getQuantity());
                        productDAO.update(effectiveProduct);
                    }
                }
            }
        } else {
            for (SaleItem item : items) {
                Product product = productDAO.findById(item.getItemId());
                if (product == null) continue;
                if ("STOCK".equalsIgnoreCase(product.getProductSource())) {
                    int effectiveId = getEffectiveProductId(product);
                    Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                    if (effectiveProduct != null) {
                        effectiveProduct.setStock(effectiveProduct.getStock() + item.getQuantity());
                        productDAO.update(effectiveProduct);
                    }
                } else if (item.getBatchId() != null && item.getBatchId() > 0) {
                    int effectiveId = getEffectiveProductId(product);
                    purchaseDAO.addBalanceQuantity(effectiveId, item.getBatchId(), item.getQuantity());
                    Product effectiveProduct = effectiveId == product.getId() ? product : productDAO.findById(effectiveId);
                    if (effectiveProduct != null && effectiveProduct.getCurrentBatchId() == item.getBatchId()) {
                        double cb = purchaseDAO.getBalanceQuantity(effectiveId, item.getBatchId());
                        effectiveProduct.setStock(cb);
                        productDAO.update(effectiveProduct);
                    }
                }
            }
        }

        saleItemDAO.deleteBySaleId(saleId);
        saleDAO.delete(saleId);
    }

    private List<Object[]> getBatchConsumptions(int saleId) throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT item_id, batch_id, consumed_quantity FROM sale_batch_consumption WHERE sale_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(new Object[]{rs.getInt("item_id"), rs.getInt("batch_id"), rs.getDouble("consumed_quantity")});
            }
        }
        return result;
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
            return product.getStock();
        }

        int effectiveId = getEffectiveProductId(product);
        double totalPurchased = purchaseDAO.getTotalPurchasedQuantity(effectiveId);
        double totalSold = saleItemDAO.getTotalSoldQuantityIncludingChildren(effectiveId);
        return totalPurchased - totalSold;
    }

    public List<Object[]> getItemSales(LocalDate fromDate, LocalDate toDate, Integer productId, Integer batchId) throws SQLException {
        return saleItemDAO.getItemSales(fromDate, toDate, productId, batchId);
    }

    public List<Integer> getDistinctBatches(LocalDate fromDate, LocalDate toDate) throws SQLException {
        return saleItemDAO.getDistinctBatches(fromDate, toDate);
    }

    public List<Integer> getDistinctBatchesForProduct(int productId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        return saleItemDAO.getDistinctBatchesForProduct(productId, fromDate, toDate);
    }

    public double getTotalCashByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.getTotalCashByDateRange(startDate, endDate);
    }

    public double getTotalGPayByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.getTotalGPayByDateRange(startDate, endDate);
    }

}
