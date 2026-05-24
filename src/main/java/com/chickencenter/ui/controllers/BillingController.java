package com.chickencenter.ui.controllers;

import com.chickencenter.model.Product;
import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;
import com.chickencenter.model.Account;
import com.chickencenter.printer.PrinterSettingsManager;
import com.chickencenter.printer.ReceiptPrintTask;
import com.chickencenter.printer.ThermalReceiptBuilder;
import com.chickencenter.service.AccountService;
import com.chickencenter.service.BillingService;
import com.chickencenter.service.ProductService;
import com.chickencenter.util.DropdownUtils;
import com.chickencenter.util.TableUtils;
import com.chickencenter.util.ToastManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

public class BillingController {
    @FXML
    private TableView<SaleItem> tblCart;
    @FXML
    private TableColumn<SaleItem, String> colCartItem;
    @FXML
    private TableColumn<SaleItem, Double> colCartQty;
    @FXML
    private TableColumn<SaleItem, Double> colCartPrice;
    @FXML
    private TableColumn<SaleItem, Double> colCartDiscount;
    @FXML
    private TableColumn<SaleItem, Double> colCartFinal;
    @FXML
    private TableColumn<SaleItem, Void> actionCol;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtAmount;
    @FXML
    private TextField txtDiscount;
    @FXML
    private Label lblTotalAmount;
    @FXML
    private Label lblPrice;
    @FXML
    private ComboBox<Product> cmbProduct;
    @FXML
    private Label lblHelperText;
    @FXML
    private Button btnAddToCart;
    @FXML
    private Button btnClearCart;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSavePrint;
    @FXML
    private ComboBox<String> cmbPaymentMode;
    @FXML
    private VBox cashFieldBox;
    @FXML
    private VBox gpayFieldBox;
    @FXML
    private TextField txtCashAmount;
    @FXML
    private TextField txtGpayAmount;

    private final BillingService billingService;
    private final ProductService productService;
    private final ObservableList<SaleItem> cartList;
    private final ObservableList<Product> productList;
    private Sale currentSale;
    private Product selectedProduct;
    private double actualPrice;
    private boolean isUpdating = false;
    private boolean enterKeyPressed = false;

    public BillingController() {
        this.billingService = new BillingService();
        this.productService = new ProductService();
        this.cartList = FXCollections.observableArrayList();
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        setupCartTable();
        setupAutoCalculation();
        setupPaymentMode();
        loadProducts();
        cmbProduct.setButtonCell(new ListCell<Product>() {
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Product");
                } else {
                    setText(getProductDisplayName(item));
                }
            }
        });
        cmbProduct.getSelectionModel().clearSelection();
        cmbProduct.setValue(null);
        if (cmbProduct.getEditor() != null) {
            cmbProduct.getEditor().clear();
        }
        cmbProduct.setPromptText("Choose Product");
        enterKeyPressed = false;
        cmbProduct.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                enterKeyPressed = true;
                handleProductSelection();
                e.consume();
            }
        });
        cmbProduct.setOnHiding(e -> {
            if (!enterKeyPressed) {
                handleProductSelection();
            }
            enterKeyPressed = false;
        });
        DropdownUtils.makeScrollable(cmbProduct);
        DropdownUtils.makeScrollable(cmbPaymentMode);
        resetForm();
    }

    private void handleProductSelection() {
        Product selected = cmbProduct.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        clearProductInfo();
        loadProductInfo(selected);
    }

    private void clearProductInfo() {
        lblPrice.setText("Rs. 0.00");
    }

    private void loadProductInfo(Product product) {
        if (product == null) return;
        selectedProduct = product;
        actualPrice = product.getPrice();
        lblPrice.setText("Rs. " + String.format("%.2f", actualPrice));
        txtQuantity.requestFocus();
    }

    private void setupCartTable() {
        tblCart.setSelectionModel(null);
        tblCart.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableUtils.addSerialNumberColumn(tblCart, 0);
        colCartItem.setCellValueFactory(cellData -> {
            String itemName = getProductName(cellData.getValue().getItemId());
            return new javafx.beans.property.SimpleStringProperty(itemName);
        });
        colCartQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getQuantity()).asObject());
        colCartPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colCartDiscount.setCellValueFactory(cellData -> {
            double disc = cellData.getValue().getDiscountAmount();
            return new javafx.beans.property.SimpleDoubleProperty(disc > 0 ? disc : 0).asObject();
        });
        colCartFinal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        colCartItem.setStyle("-fx-alignment: CENTER-LEFT;");
        colCartQty.setStyle("-fx-alignment: CENTER;");
        colCartPrice.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCartDiscount.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCartFinal.setStyle("-fx-alignment: CENTER-RIGHT;");
        actionCol.setCellFactory(param -> new TableCell<SaleItem, Void>() {
            private final Button removeBtn = new Button("X");

            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");
                removeBtn.setOnAction(event -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    removeItemFromBill(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                }
            }
        });
        tblCart.setItems(cartList);
    }

    private void setupAutoCalculation() {
        setupNumericField(txtQuantity);
        setupNumericField(txtAmount);
        setupNumericField(txtDiscount);
        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating || !txtQuantity.isFocused() || newVal == null || newVal.isEmpty() || actualPrice <= 0)
                return;
            try {
                isUpdating = true;
                double qty = Double.parseDouble(newVal);
                double effectivePrice = getEffectivePrice(selectedProduct, qty);
                double amt = qty * effectivePrice;
                if (effectivePrice != actualPrice) {
                    lblPrice.setText("Rs. " + String.format("%.2f", effectivePrice) + " (Bulk)");
                } else {
                    lblPrice.setText("Rs. " + String.format("%.2f", actualPrice));
                }
                txtAmount.setText(String.format("%.2f", amt));
            } catch (NumberFormatException e) {
            } finally {
                isUpdating = false;
            }
        });
        txtAmount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating || !txtAmount.isFocused() || newVal == null || newVal.isEmpty() || actualPrice <= 0) return;
            try {
                isUpdating = true;
                double qty = Double.parseDouble(txtQuantity.getText().isEmpty() ? "0" : txtQuantity.getText());
                double effectivePrice = getEffectivePrice(selectedProduct, qty);
                double amt = Double.parseDouble(newVal);
                double calcQty = amt / effectivePrice;
                txtQuantity.setText(String.format("%.2f", calcQty));
            } catch (NumberFormatException e) {
            } finally {
                isUpdating = false;
            }
        });

        txtQuantity.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addToCart();
            }
        });
        txtAmount.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addToCart();
            }
        });
        txtDiscount.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addToCart();
            }
        });
    }

    private double getEffectivePrice(Product product, double quantity) {
        if (product == null) return actualPrice;
        double threshold = product.getBulkThreshold();
        double bulkPrice = product.getBulkPrice();
        System.out.println("[BulkCheck] Product: " + product.getProductName() + " | Qty: " + quantity + " | Threshold: " + threshold + " | BulkPrice: " + bulkPrice + " | NormalPrice: " + actualPrice);
        if (threshold > 0 && bulkPrice > 0 && quantity > threshold) {
            System.out.println("[BulkCheck] BULK APPLIED: " + bulkPrice);
            return bulkPrice;
        }
        System.out.println("[BulkCheck] NORMAL PRICE: " + actualPrice);
        return actualPrice;
    }

    private void setupNumericField(TextField field) {
        TextFormatter<Double> formatter = new TextFormatter<>(new DoubleStringConverter(), 0.0, change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("\\d*(\\.\\d{0,2})?")) return change;
            return null;
        });
        field.setTextFormatter(formatter);
    }

    private void setupPaymentMode() {
        cmbPaymentMode.getItems().addAll("Cash", "GPay", "Both");
        cmbPaymentMode.setValue("Cash");
        cmbPaymentMode.setOnAction(e -> handlePaymentModeChange());
        handlePaymentModeChange();
        setupNumericField(txtCashAmount);
        txtCashAmount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                txtGpayAmount.setText("0.00");
                return;
            }
            try {
                double cash = Double.parseDouble(newVal);
                double total = calculateCartTotal();
                if (cash > total) {
                    showError("Cash amount cannot exceed total amount");
                    txtCashAmount.setText(oldVal != null ? oldVal : "0");
                    return;
                }
                double gpay = total - cash;
                txtGpayAmount.setText(String.format("%.2f", Math.max(0, gpay)));
            } catch (NumberFormatException e) {
                txtGpayAmount.setText("0.00");
            }
        });
    }

    private void handlePaymentModeChange() {
        String mode = cmbPaymentMode.getValue();
        if (mode == null) mode = "Cash";
        double total = calculateCartTotal();
        switch (mode) {
            case "Cash":
                cashFieldBox.setVisible(false);
                cashFieldBox.setManaged(false);
                gpayFieldBox.setVisible(false);
                gpayFieldBox.setManaged(false);
                break;
            case "GPay":
                cashFieldBox.setVisible(false);
                cashFieldBox.setManaged(false);
                gpayFieldBox.setVisible(true);
                gpayFieldBox.setManaged(true);
                txtGpayAmount.setText(String.format("%.2f", total));
                break;
            case "Both":
                cashFieldBox.setVisible(true);
                cashFieldBox.setManaged(true);
                gpayFieldBox.setVisible(true);
                gpayFieldBox.setManaged(true);
                txtCashAmount.clear();
                txtGpayAmount.setText("0.00");
                break;
        }
    }

    private String getProductName(int productId) {
        for (Product p : productList) {
            if (p.getId() == productId) return getProductDisplayName(p);
        }
        return "";
    }

    private String getProductDisplayName(Product p) {
        if (p.getParentProductId() != null) {
            for (Product prod : productList) {
                if (prod.getId() == p.getParentProductId()) {
                    return p.getProductName() + " (" + prod.getProductName() + ")";
                }
            }
        }
        return p.getProductName();
    }

    private void loadProducts() {
        try {
            productList.clear();
            cmbProduct.getItems().clear();
            var products = productService.getAllProducts();
            productList.addAll(products);
            cmbProduct.getItems().addAll(products);
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    @FXML
    private void onProductSelected() {
        selectedProduct = cmbProduct.getValue();
        if (selectedProduct == null) {
            actualPrice = 0;
            lblPrice.setText("Rs. 0.00");
            return;
        }
        actualPrice = selectedProduct.getPrice();
        lblPrice.setText("Rs. " + String.format("%.2f", actualPrice));
        txtQuantity.clear();
        txtAmount.clear();
        txtDiscount.clear();
        txtQuantity.requestFocus();
    }

    private void resetForm() {
        currentSale = null;
        cartList.clear();
        updateTotal();
        loadProducts();
        cmbProduct.setButtonCell(new ListCell<Product>() {
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Product");
                } else {
                    setText(getProductDisplayName(item));
                }
            }
        });
        cmbProduct.getSelectionModel().clearSelection();
        cmbProduct.setValue(null);
        if (cmbProduct.getEditor() != null) cmbProduct.getEditor().clear();
        cmbProduct.setPromptText("Choose Product");
        selectedProduct = null;
        actualPrice = 0;
        lblPrice.setText("Rs. 0.00");
        txtQuantity.clear();
        txtAmount.clear();
        txtDiscount.clear();
        cmbPaymentMode.setValue("Cash");
        txtCashAmount.clear();
        txtGpayAmount.clear();
        handlePaymentModeChange();
    }

    @FXML
    private void addToCart() {
        btnAddToCart.setDisable(true);
        try {
            if (selectedProduct == null) {
                showError("Please select a product");
                return;
            }
            String qtyText = txtQuantity.getText().trim();
            String amtText = txtAmount.getText().trim();
            if (qtyText.isEmpty()) {
                showError("Quantity is required");
                txtQuantity.requestFocus();
                return;
            }
            if (amtText.isEmpty()) {
                showError("Amount is required");
                txtAmount.requestFocus();
                return;
            }
            double quantity;
            double amount;
            try {
                quantity = Double.parseDouble(qtyText);
                amount = Double.parseDouble(amtText);
                if (quantity <= 0) {
                    showError("Quantity must be greater than zero");
                    txtQuantity.requestFocus();
                    return;
                }
                if (amount <= 0) {
                    showError("Amount must be greater than zero");
                    txtAmount.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid numeric value in quantity or amount");
                return;
            }

            double discount = 0;
            String discText = txtDiscount.getText().trim();
            if (!discText.isEmpty()) {
                try {
                    discount = Double.parseDouble(discText);
                    if (discount < 0) {
                        showError("Discount cannot be negative");
                        txtDiscount.requestFocus();
                        return;
                    }
                    if (discount > amount) {
                        showError("Discount cannot exceed item amount");
                        txtDiscount.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid numeric value in discount");
                    return;
                }
            }

            double effectivePrice = getEffectivePrice(selectedProduct, quantity);
            int batchId = selectedProduct.getCurrentBatchId();
            if (selectedProduct.getParentProductId() != null && selectedProduct.getParentProductId() > 0) {
                Product parent = productService.getProduct(selectedProduct.getParentProductId());
                if (parent != null) batchId = parent.getCurrentBatchId();
            }
            SaleItem saleItem = new SaleItem(0, selectedProduct.getId(), batchId, quantity, amount, effectivePrice);
            saleItem.setDiscountAmount(discount);
            saleItem.setId(-(cartList.size() + 1));
            cartList.add(saleItem);
            txtQuantity.clear();
            txtAmount.clear();
            txtDiscount.clear();
            updateTotal();
            handlePaymentModeChange();
            cmbProduct.requestFocus();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Error adding item: " + e.getMessage());
        } finally {
            btnAddToCart.setDisable(false);
        }
    }

    @FXML
    private void clearFields() {
        txtQuantity.clear();
        txtAmount.clear();
        txtDiscount.clear();
        txtQuantity.requestFocus();
    }

    @FXML
    private void clearCart() {
        resetForm();
    }

    private void removeItemFromBill(SaleItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Item");
        alert.setHeaderText(null);
        alert.setContentText("Remove this item from bill?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cartList.remove(item);
                updateTotal();
                handlePaymentModeChange();
            }
        });
    }

    @FXML
    private void saveSale() {
        if (!validateBillingForm()) return;
        double total = calculateCartTotal();
        if (total <= 0) {
            showError("Invalid bill amount");
            return;
        }
        btnSave.setDisable(true);
        btnSavePrint.setDisable(true);
        try {
            String paymentMode = cmbPaymentMode.getValue();
            double cashAmount = 0, gpayAmount = 0;
            switch (paymentMode) {
                case "Cash":
                    cashAmount = total;
                    gpayAmount = 0;
                    break;
                case "GPay":
                    cashAmount = 0;
                    gpayAmount = total;
                    break;
                case "Both":
                    String cashText = txtCashAmount.getText().trim();
                    if (cashText.isEmpty()) {
                        showError("Enter cash amount");
                        return;
                    }
                    cashAmount = Double.parseDouble(cashText);
                    if (cashAmount <= 0) {
                        showError("Cash amount must be greater than zero");
                        return;
                    }
                    if (cashAmount > total) {
                        showError("Cash amount cannot exceed total amount");
                        return;
                    }
                    gpayAmount = total - cashAmount;
                    break;
            }

            currentSale = billingService.createSale(total, LocalDate.now());
            billingService.saveSaleItems(currentSale.getId(), new ArrayList<>(cartList));
            billingService.completeSaleWithPayment(currentSale.getId(), false, paymentMode, cashAmount, gpayAmount);
            ToastManager.showSuccess("Sale saved successfully!");
            resetForm();
        } catch (Exception e) {
            showError("Error saving sale: " + e.getMessage());
            if (currentSale != null && currentSale.getId() > 0) {
                try { billingService.deleteSale(currentSale.getId()); } catch (SQLException ignored) {}
            }
            resetForm();
        } finally {
            btnSave.setDisable(false);
            btnSavePrint.setDisable(false);
        }
    }

    @FXML
    private void saveAndPrintSale() {
        if (!validateBillingForm()) return;
        if (!validatePrinter()) return;
        double total = calculateCartTotal();
        if (total <= 0) {
            showError("Invalid bill amount");
            return;
        }
        btnSave.setDisable(true);
        btnSavePrint.setDisable(true);
        try {
            String paymentMode = cmbPaymentMode.getValue();
            double cashAmount = 0, gpayAmount = 0;
            switch (paymentMode) {
                case "Cash":
                    cashAmount = total;
                    gpayAmount = 0;
                    break;
                case "GPay":
                    cashAmount = 0;
                    gpayAmount = total;
                    break;
                case "Both":
                    String cashText = txtCashAmount.getText().trim();
                    if (cashText.isEmpty()) {
                        showError("Enter cash amount");
                        return;
                    }
                    cashAmount = Double.parseDouble(cashText);
                    if (cashAmount <= 0) {
                        showError("Cash amount must be greater than zero");
                        return;
                    }
                    if (cashAmount > total) {
                        showError("Cash amount cannot exceed total amount");
                        return;
                    }
                    gpayAmount = total - cashAmount;
                    break;
            }

            currentSale = billingService.createSale(total, LocalDate.now());
            billingService.saveSaleItems(currentSale.getId(), new ArrayList<>(cartList));

            AccountService acctSvc = new AccountService();
            Account account = acctSvc.getAccount();
            String shopName = account != null ? account.getShopName() : "JK CHICKEN CENTER";
            String shopAddress = account != null ? account.getShopAddress() : "";
            String shopPhone = account != null ? account.getContactNo1() : "";

            java.util.List<SaleItem> dbItems = billingService.getSaleItems(currentSale.getId());
            Sale sale = new Sale();
            sale.setId(currentSale.getId());
            sale.setTotalAmount(total);
            sale.setSaleDate(LocalDate.now());
            sale.setPaymentMode(paymentMode);
            sale.setCashAmount(cashAmount);
            sale.setGpayAmount(gpayAmount);
            sale.setCreatedAt(LocalDateTime.now());

            billingService.completeSaleWithPayment(currentSale.getId(), true, paymentMode, cashAmount, gpayAmount);

            Map<Integer, String> productNames = new HashMap<>();
            for (Product p : productList) {
                productNames.put(p.getId(), getProductDisplayName(p));
            }
            ThermalReceiptBuilder receiptBuilder = new ThermalReceiptBuilder();
            byte[] receiptData = receiptBuilder.buildReceipt(sale, dbItems, shopName, shopAddress, shopPhone, productNames);

            PrinterSettingsManager psm = new PrinterSettingsManager();
            String printerName = psm.getPrinterName();
            ReceiptPrintTask printTask = new ReceiptPrintTask(receiptData, printerName,
                msg -> Platform.runLater(() -> ToastManager.showSuccess("Bill sent to printer")),
                err -> Platform.runLater(() -> showError("Print warning: " + err))
            );
            new Thread(printTask).start();

            ToastManager.showSuccess("Bill printed and saved successfully!");
            resetForm();
        } catch (Exception e) {
            showError("Error saving sale: " + e.getMessage());
            if (currentSale != null && currentSale.getId() > 0) {
                try { billingService.deleteSale(currentSale.getId()); } catch (SQLException ignored) {}
            }
            resetForm();
        } finally {
            btnSave.setDisable(false);
            btnSavePrint.setDisable(false);
        }
    }

    private boolean validateBillingForm() {
        if (cartList.isEmpty()) {
            showError("Cart is empty");
            return false;
        }
        if (cmbPaymentMode.getValue() == null || cmbPaymentMode.getValue().isEmpty()) {
            showError("Please select payment mode");
            return false;
        }
        return true;
    }

    private boolean validatePrinter() {
        PrinterSettingsManager psm = new PrinterSettingsManager();
        String printerName = psm.getPrinterName();
        if (printerName == null || printerName.isEmpty()) {
            showError("No printer configured. Go to Account Settings to select a printer.");
            return false;
        }
        if (!psm.isPrinterAvailable(printerName)) {
            showError("Printer \"" + printerName + "\" is not connected or unavailable.");
            return false;
        }
        return true;
    }

    private double calculateCartTotal() {
        return cartList.stream().mapToDouble(SaleItem::getTotal).sum();
    }

    // Thermal receipt implemented via ThermalReceiptBuilder + PrinterService

    private void updateTotal() {
        double total = cartList.stream().mapToDouble(SaleItem::getTotal).sum();
        lblTotalAmount.setText("Rs." + String.format("%.2f", total));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
