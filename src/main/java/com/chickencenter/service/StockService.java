package com.chickencenter.service;

import com.chickencenter.dao.ProductDAO;
import com.chickencenter.dao.PurchaseDAO;
import com.chickencenter.dao.SaleItemDAO;
import com.chickencenter.model.Product;

import java.sql.SQLException;

public class StockService {
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;
    private final SaleItemDAO saleItemDAO;

    public StockService() {
        this.productDAO = new ProductDAO();
        this.purchaseDAO = new PurchaseDAO();
        this.saleItemDAO = new SaleItemDAO();
    }

    public double getAvailableStock(int productId) throws SQLException {
        return getAvailableStock(productId, 0);
    }

    public double getAvailableStock(int productId, int batchId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) return 0;

        if ("STOCK".equalsIgnoreCase(product.getProductSource())) {
            System.out.println("[StockService] Product: " + product.getProductName() + " | ID: " + productId + " | Source: STOCK | Stock: " + product.getStock());
            return product.getStock();
        }

        int effectiveId = product.getParentProductId() != null && product.getParentProductId() > 0
            ? product.getParentProductId() : product.getId();

        Product effectiveProduct = effectiveId == productId ? product : productDAO.findById(effectiveId);
        int activeBatchId = batchId > 0 ? batchId : (effectiveProduct != null ? effectiveProduct.getCurrentBatchId() : 0);

        double totalPurchased = purchaseDAO.getTotalPurchasedQuantity(effectiveId);
        double totalSold = saleItemDAO.getTotalSoldQuantityIncludingChildren(effectiveId);

        if (activeBatchId > 0) {
            double batchBalance = purchaseDAO.getBalanceQuantity(effectiveId, activeBatchId);
            System.out.println("[StockService] Product: " + product.getProductName() + " | ID: " + productId + " | EffectiveID: " + effectiveId + " | BatchID: " + activeBatchId + " | TotalPurchased: " + totalPurchased + " | TotalSold: " + totalSold + " | BatchBalance: " + batchBalance + " | Available: " + batchBalance);
            return batchBalance;
        }

        System.out.println("[StockService] Product: " + product.getProductName() + " | ID: " + productId + " | EffectiveID: " + effectiveId + " | NoBatch | TotalPurchased: " + totalPurchased + " | TotalSold: " + totalSold + " | Available: " + (totalPurchased - totalSold));
        return totalPurchased - totalSold;
    }
}
