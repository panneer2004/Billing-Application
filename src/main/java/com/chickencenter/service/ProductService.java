package com.chickencenter.service;

import com.chickencenter.dao.ProductDAO;
import com.chickencenter.dao.PurchaseDAO;
import com.chickencenter.dao.SaleItemDAO;
import com.chickencenter.dao.VendorDAO;
import com.chickencenter.database.DatabaseConnection;
import com.chickencenter.model.Product;
import com.chickencenter.model.Purchase;
import com.chickencenter.model.Vendor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;
    private final VendorDAO vendorDAO;
    private final SaleItemDAO saleItemDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.purchaseDAO = new PurchaseDAO();
        this.vendorDAO = new VendorDAO();
        this.saleItemDAO = new SaleItemDAO();
    }

    public int getEffectiveProductId(Product product) {
        return product.getParentProductId() != null && product.getParentProductId() > 0
               ? product.getParentProductId() : product.getId();
    }

    private int getEffectiveProductId(int productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) return productId;
        return getEffectiveProductId(product);
    }

    public double getCurrentBatchBalance(int productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) return 0;
        int effectiveId = getEffectiveProductId(product);
        Product effectiveProduct = effectiveId == productId ? product : productDAO.findById(effectiveId);
        if (effectiveProduct == null) return 0;
        int batchId = effectiveProduct.getCurrentBatchId();
        if (batchId <= 0) return 0;
        return purchaseDAO.getBalanceQuantity(effectiveId, batchId);
    }

    public int createProduct(String productName, String unit, int vendorId, double price) throws SQLException {
        Product product = new Product(productName, unit, vendorId);
        product.setCurrentBatchId(0);
        product.setStock(0);
        product.setPrice(price);
        return productDAO.create(product);
    }

    public int createProduct(Product product) throws SQLException {
        product.setCurrentBatchId(0);
        product.setStock(0);
        return productDAO.create(product);
    }

    public void updateProduct(Product product) throws SQLException {
        productDAO.update(product);
    }

    public void deleteProduct(int id) throws SQLException {
        // Check if product is used in sale_items
        String checkSql = "SELECT COUNT(*) FROM sale_items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete product because billing history exists in sale_items");
            }
        }
        
        // Check if product is in purchases
        checkSql = "SELECT COUNT(*) FROM purchases WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete product because purchase history exists");
            }
        }
        
        // Soft delete - set is_active = 0
        productDAO.delete(id);
    }

    public boolean batchExists(int productId, int batchId) throws SQLException {
        return productDAO.batchExists(getEffectiveProductId(productId), batchId);
    }

    public void updateBatch(int productId, int newBatchId) throws SQLException {
        productDAO.updateBatch(getEffectiveProductId(productId), newBatchId);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public List<PurchaseDAO.ProductWithVendor> getAllProductsWithVendor() throws SQLException {
        return purchaseDAO.findAllWithVendor();
    }

    public List<Product> getProductsByVendor(int vendorId) throws SQLException {
        return productDAO.findByVendorId(vendorId);
    }

    public Product getProduct(int id) throws SQLException {
        return productDAO.findById(id);
    }

    public List<Vendor> getAllVendors() throws SQLException {
        return vendorDAO.findAll();
    }

    public void recordSale(int productId, double quantity) throws SQLException {
        int effectiveId = getEffectiveProductId(productId);
        Product product = productDAO.findById(effectiveId);
        if (product == null) return;

        double newStock = product.getStock() - quantity;
        
        if (newStock < 0) {
            throw new SQLException("Insufficient stock!");
        }

        product.setStock(newStock);
        
        if (product.getCurrentBatchId() == 0) {
            product.setCurrentBatchId(1);
        }
        
        productDAO.update(product);
    }

    public void addStock(int productId, double quantity) throws SQLException {
        int effectiveId = getEffectiveProductId(productId);
        Product product = productDAO.findById(effectiveId);
        if (product == null) return;

        if (product.getStock() <= 0 && product.getCurrentBatchId() > 0) {
            product.setCurrentBatchId(product.getCurrentBatchId() + 1);
        } else if (product.getCurrentBatchId() == 0) {
            product.setCurrentBatchId(1);
        }

        product.setStock(product.getStock() + quantity);
        productDAO.update(product);
    }

    public void makePurchase(int productId, double quantity, double rate, double totalAmount) throws SQLException {
        System.out.println("makePurchase called - productId: " + productId + ", quantity: " + quantity + ", rate: " + rate + ", totalAmount: " + totalAmount);
        
        Product product = productDAO.findById(productId);
        if (product == null) {
            System.out.println("Product not found for id: " + productId);
            return;
        }
        
        System.out.println("Product found - vendorId: " + product.getVendorId());

        Purchase purchase = new Purchase(productId, product.getVendorId(), quantity, rate, totalAmount);
        purchase.setBalanceQuantity(quantity);
        System.out.println("Purchase object created, calling DAO.create");
        
        int purchaseId = purchaseDAO.create(purchase);
        System.out.println("Purchase created with ID: " + purchaseId);

        Purchase created = purchaseDAO.findById(purchaseId);
        if (created != null && product.getCurrentBatchId() == 0) {
            product.setCurrentBatchId(created.getItemBatchId());
            product.setStock(quantity);
            productDAO.update(product);
        }
    }

    public Product getNextBatch(int productId, int currentBatchId) throws SQLException {
        int effectiveId = getEffectiveProductId(productId);
        Integer nextBatchId = purchaseDAO.getNextBatchId(effectiveId, currentBatchId);
        if (nextBatchId != null) {
            Product product = productDAO.findById(effectiveId);
            product.setCurrentBatchId(nextBatchId);
            product.setStock(purchaseDAO.getBalanceQuantity(effectiveId, nextBatchId));
            productDAO.update(product);
            return productDAO.findById(effectiveId);
        }
        return null;
    }

    public Product getPrevBatch(int productId, int currentBatchId) throws SQLException {
        if (currentBatchId > 0) {
            int effectiveId = getEffectiveProductId(productId);
            Product product = productDAO.findById(effectiveId);
            product.setCurrentBatchId(currentBatchId - 1);
            product.setStock(purchaseDAO.getBalanceQuantity(effectiveId, currentBatchId - 1));
            productDAO.update(product);
            return productDAO.findById(effectiveId);
        }
        return null;
    }

    public boolean autoSwitchBatchIfExhausted(int productId) throws SQLException {
        int effectiveId = getEffectiveProductId(productId);
        Product product = productDAO.findById(effectiveId);
        if (product == null) return false;
        int currentBatchId = product.getCurrentBatchId();
        if (currentBatchId <= 0) return false;
        double currentBalance = purchaseDAO.getBalanceQuantity(effectiveId, currentBatchId);
        if (currentBalance > 0) return false;
        Integer nextBatchId = purchaseDAO.getNextBatchWithPositiveBalance(effectiveId, currentBatchId);
        if (nextBatchId != null) {
            double newBalance = purchaseDAO.getBalanceQuantity(effectiveId, nextBatchId);
            product.setCurrentBatchId(nextBatchId);
            product.setStock(newBalance);
            productDAO.update(product);
            return true;
        } else {
            product.setStock(0);
            productDAO.update(product);
            return false;
        }
    }

    public double getBatchStock(int productId, int batchId) throws SQLException {
        return purchaseDAO.getBatchStock(getEffectiveProductId(productId), batchId);
    }

    public double getBatchBalanceQuantity(int productId, int batchId) throws SQLException {
        return purchaseDAO.getBalanceQuantity(getEffectiveProductId(productId), batchId);
    }
    
    public double getTotalAvailableStock(int productId) throws SQLException {
        int effectiveId = getEffectiveProductId(productId);
        double totalPurchased = purchaseDAO.getTotalPurchasedQuantity(effectiveId);
        double totalSold = saleItemDAO.getTotalSoldQuantityIncludingChildren(effectiveId);
        return totalPurchased - totalSold;
    }

    public void updatePurchase(int purchaseId, double quantity, double rate, double totalAmount) throws SQLException {
        Purchase purchase = new Purchase();
        purchase.setId(purchaseId);
        purchase.setBatchQuantity(quantity);
        purchase.setRate(rate);
        purchase.setTotalAmount(totalAmount);
        purchaseDAO.update(purchase);
    }

    public void deletePurchase(int purchaseId) throws SQLException {
        purchaseDAO.delete(purchaseId);
    }

    public List<com.chickencenter.dao.PurchaseDAO.PurchaseWithDetails> getAllPurchaseDetails() throws SQLException {
        return purchaseDAO.findAllWithDetails();
    }
    
    public List<com.chickencenter.dao.PurchaseDAO.PurchaseWithDetails> getPurchaseDetailsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return purchaseDAO.findAllWithDetailsByDateRange(startDate, endDate);
    }

    public List<Product> getAllBatches() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> getAllParentProducts() throws SQLException {
        return productDAO.findAllParents();
    }

    public boolean hasChildren(int parentId) throws SQLException {
        return productDAO.hasChildren(parentId);
    }
}
