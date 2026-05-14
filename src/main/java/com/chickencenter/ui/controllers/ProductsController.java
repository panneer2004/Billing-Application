package com.chickencenter.ui.controllers;

import com.chickencenter.model.Product;
import com.chickencenter.model.Vendor;
import com.chickencenter.service.ProductService;
import com.chickencenter.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProductsController {
    @FXML
    private TableView<Product> tblProducts;
    @FXML
    private TableColumn<Product, String> colProductName;
    @FXML
    private TableColumn<Product, String> colVendor;
    @FXML
    private TableColumn<Product, String> colStock;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colBatchId;
    @FXML
    private TableColumn<Product, Boolean> colBatchControl;
    @FXML
    private TableColumn<Product, String> colCreatedAt;
    @FXML
    private TableColumn<Product, String> colLastModified;
    @FXML
    private TableColumn<Product, Boolean> colAction;
    @FXML
    private TextField txtProductName;
    @FXML
    private TextField txtSearch;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtBulkThreshold;
    @FXML
    private TextField txtBulkPrice;
    @FXML
    private ComboBox<String> cmbUnit;
    @FXML
    private ComboBox<Vendor> cmbVendor;
    @FXML
    private CheckBox chkSubProduct;
    @FXML
    private ComboBox<Product> cmbParentProduct;
    @FXML
    private VBox parentProductContainer;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnClear;
    private final ProductService productService;
    private final ObservableList<Product> productList;
    private final ObservableList<Vendor> vendorList;
    private Product selectedProduct;
    private boolean isEditMode = false;

    public ProductsController() {
        this.productService = new ProductService();
        this.productList = FXCollections.observableArrayList();
        this.vendorList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadVendors();
        loadProducts();
        loadParentProducts();
        setupAlphabetField(txtProductName);
        setupNumericField(txtPrice);
        chkSubProduct.selectedProperty().addListener((obs, oldVal, newVal) -> {
            parentProductContainer.setVisible(newVal);
            parentProductContainer.setManaged(newVal);
            if (!newVal) {
                cmbParentProduct.setValue(null);
            }
        });
        cmbParentProduct.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    Product parent = productService.getProduct(newVal.getId());
                    if (parent != null) {
                        for (Vendor v : vendorList) {
                            if (v.getId() == parent.getVendorId()) {
                                cmbVendor.setValue(v);
                                cmbVendor.setDisable(true);
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    showError("Error loading parent vendor: " + e.getMessage());
                }
            } else {
                cmbVendor.setValue(null);
                cmbVendor.setDisable(false);
            }
        });
        cmbParentProduct.setButtonCell(new ListCell<Product>() {
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Parent Product" : item.getProductName());
            }
        });
        cmbParentProduct.setPromptText("Select Parent Product");
        cmbUnit.getItems().addAll("KG", "Piece");
        cmbUnit.setButtonCell(new ListCell<String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Unit");
                } else {
                    setText(item);
                }
            }
        });
        cmbUnit.setPromptText("Choose Unit");
        cmbVendor.setButtonCell(new ListCell<Vendor>() {
            protected void updateItem(Vendor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Vendor");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbVendor.setPromptText("Choose Vendor");
        tblProducts.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setPrefHeight(44);
            return row;
        });
    }

    private void setupAlphabetField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z ]*")) {
                field.setText(oldVal);
            }
        });
    }

    private void setupNumericField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*(\\.\\d{0,2})?")) {
                field.setText(oldVal);
            }
        });
    }

    private void setupTable() {
        tblProducts.setSelectionModel(null);
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colProductName.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Product p = getTableRow().getItem();
                    if (p.getParentProductId() != null) {
                        setText("    " + getProductDisplayName(p));
                        setStyle("-fx-padding: 4 4 4 20;");
                    } else {
                        setText(p.getProductName());
                        setStyle("-fx-padding: 4 4 4 8; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colVendor.setCellValueFactory(cellData -> {
            try {
                int vendorId = cellData.getValue().getVendorId();
                List<Vendor> vendors = productService.getAllVendors();
                for (Vendor v : vendors) {
                    if (v.getId() == vendorId) {
                        return new javafx.beans.property.SimpleStringProperty(v.getName());
                    }
                }
                return new javafx.beans.property.SimpleStringProperty("");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        colStock.setCellFactory(col -> new TableCell<Product, String>() {
            {
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Product product = getTableRow().getItem();
                    double stock;
                    try {
                        stock = productService.getTotalAvailableStock(product.getId());
                    } catch (SQLException e) {
                        stock = 0;
                    }
                    String unit = product.getUnit() != null ? product.getUnit() : "";
                    String display = stock % 1 == 0 ? String.valueOf((int) stock) : String.format("%.2f", stock);
                    setText(display + " " + unit);
                    if (stock <= 0) {
                        setTextFill(javafx.scene.paint.Paint.valueOf("#ef4444"));
                    } else if (stock <= 5) {
                        setTextFill(javafx.scene.paint.Paint.valueOf("#f59e0b"));
                    } else {
                        setTextFill(javafx.scene.paint.Paint.valueOf("#10b981"));
                    }
                }
            }
        });
        colPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getPrice();
            return new javafx.beans.property.SimpleObjectProperty<>(price);
        });
        colBatchId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCurrentBatchId()).asObject());
        colBatchControl.setCellFactory(col -> new TableCell<Product, Boolean>() {
            private final HBox hbox = new HBox(5);
            private final Button btnPrev = new Button("Prev");
            private final Button btnNext = new Button("Next");

            {
                btnPrev.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10; -fx-padding: 3 8;");
                btnNext.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10; -fx-padding: 3 8;");
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(btnPrev, btnNext);
                btnPrev.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    navigatePrevBatch(product);
                });
                btnNext.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    navigateNextBatch(product);
                });
            }

            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
        colCreatedAt.setCellValueFactory(cellData -> {
            LocalDate createdAt = cellData.getValue().getCreatedAt();
            if (createdAt != null) {
                return new javafx.beans.property.SimpleStringProperty(createdAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colLastModified.setCellValueFactory(cellData -> {
            java.time.LocalDateTime lastModified = cellData.getValue().getLastModifiedAt();
            if (lastModified != null) {
                return new javafx.beans.property.SimpleStringProperty(lastModified.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colAction.setCellFactory(col -> new TableCell<Product, Boolean>() {
            private final HBox hbox = new HBox(5);
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnEdit.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(btnEdit, btnDelete);
                btnEdit.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    loadProductToForm(product);
                });
                btnDelete.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });
            }

            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
        tblProducts.setItems(productList);
    }

    private void navigateNextBatch(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to move to the next batch?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int currentBatchId = product.getCurrentBatchId();
                int productId = product.getId();
                boolean hasNext = productService.batchExists(productId, currentBatchId + 1);
                if (hasNext) {
                    productService.updateBatch(productId, currentBatchId + 1);
                    loadProducts();
                } else {
                    showError("No next batch available. Please purchase stock.");
                }
            } catch (SQLException e) {
                showError("Error navigating batch: " + e.getMessage());
            }
        }
    }

    private void navigatePrevBatch(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to move to the previous batch?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int currentBatchId = product.getCurrentBatchId();
                int productId = product.getId();
                if (currentBatchId > 0) {
                    productService.updateBatch(productId, currentBatchId - 1);
                    loadProducts();
                } else {
                    showError("Already at first batch");
                }
            } catch (SQLException e) {
                showError("Error navigating batch: " + e.getMessage());
            }
        }
    }

    private void loadProductToForm(Product product) {
        selectedProduct = product;
        isEditMode = true;
        txtProductName.setText(product.getProductName());
        cmbUnit.setValue(product.getUnit());
        for (Vendor v : vendorList) {
            if (v.getId() == product.getVendorId()) {
                cmbVendor.setValue(v);
                break;
            }
        }
        double price = product.getPrice();
        txtPrice.setText(price > 0 ? String.valueOf(price) : "");
        double bulkThreshold = product.getBulkThreshold();
        txtBulkThreshold.setText(bulkThreshold > 0 ? String.valueOf((int) bulkThreshold) : "");
        double bulkPrice = product.getBulkPrice();
        txtBulkPrice.setText(bulkPrice > 0 ? String.valueOf(bulkPrice) : "");
        if (product.getParentProductId() != null) {
            chkSubProduct.setSelected(true);
            for (Product p : cmbParentProduct.getItems()) {
                if (p.getId() == product.getParentProductId()) {
                    cmbParentProduct.setValue(p);
                    break;
                }
            }
            cmbVendor.setDisable(true);
        } else {
            chkSubProduct.setSelected(false);
            cmbParentProduct.setValue(null);
            cmbVendor.setDisable(false);
        }
        btnSave.setText("Update");
    }

    private void loadVendors() {
        try {
            vendorList.clear();
            List<Vendor> vendors = productService.getAllVendors();
            vendorList.addAll(vendors);
            cmbVendor.setItems(vendorList);
            if (vendors.isEmpty()) {
                cmbVendor.setPromptText("No vendors - add in Vendors tab");
            } else {
                cmbVendor.setPromptText("Choose Vendor");
            }
        } catch (SQLException e) {
            showError("Error loading vendors: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            productList.clear();
            productList.addAll(productService.getAllProducts());
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void loadParentProducts() {
        try {
            ObservableList<Product> parents = FXCollections.observableArrayList();
            parents.addAll(productService.getAllParentProducts());
            cmbParentProduct.setItems(parents);
        } catch (SQLException e) {
            showError("Error loading parent products: " + e.getMessage());
        }
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

    @FXML
    private void saveProduct() {
        btnSave.setDisable(true);
        String productName = txtProductName.getText().trim();
        String unit = cmbUnit.getValue();
        Vendor vendor = cmbVendor.getValue();
        if (productName.isEmpty()) {
            showError("Please enter product name");
            btnSave.setDisable(false);
            return;
        }
        if (unit == null) {
            showError("Please select unit");
            btnSave.setDisable(false);
            return;
        }
        if (vendor == null) {
            showError("Please select vendor");
            btnSave.setDisable(false);
            return;
        }
        if (chkSubProduct.isSelected() && cmbParentProduct.getValue() == null) {
            showError("Please select parent product for sub product");
            btnSave.setDisable(false);
            return;
        }
        if (chkSubProduct.isSelected() && selectedProduct != null && cmbParentProduct.getValue() != null
                && cmbParentProduct.getValue().getId() == selectedProduct.getId()) {
            showError("Product cannot be its own parent");
            btnSave.setDisable(false);
            return;
        }
        try {
            double price = 0;
            try {
                if (!txtPrice.getText().trim().isEmpty()) {
                    price = Double.parseDouble(txtPrice.getText().trim());
                }
            } catch (NumberFormatException e) {
                price = 0;
            }
            double bulkThreshold = 0;
            try {
                if (!txtBulkThreshold.getText().trim().isEmpty()) {
                    bulkThreshold = Double.parseDouble(txtBulkThreshold.getText().trim());
                }
            } catch (NumberFormatException e) {
                bulkThreshold = 0;
            }
            double bulkPrice = 0;
            try {
                if (!txtBulkPrice.getText().trim().isEmpty()) {
                    bulkPrice = Double.parseDouble(txtBulkPrice.getText().trim());
                }
            } catch (NumberFormatException e) {
                bulkPrice = 0;
            }
            if (selectedProduct != null && isEditMode) {
                selectedProduct.setProductName(productName);
                selectedProduct.setUnit(unit);
                selectedProduct.setVendorId(vendor.getId());
                selectedProduct.setPrice(price);
                selectedProduct.setBulkThreshold(bulkThreshold);
                selectedProduct.setBulkPrice(bulkPrice);
                selectedProduct.setParentProductId(chkSubProduct.isSelected() ? cmbParentProduct.getValue().getId() : null);
                productService.updateProduct(selectedProduct);
            } else {
                List<Product> existingProducts = productService.getAllProducts();
                for (Product p : existingProducts) {
                    if (p.getProductName().equalsIgnoreCase(productName) && p.getVendorId() == vendor.getId()) {
                        showError("Product already exists for this vendor!");
                        btnSave.setDisable(false);
                        return;
                    }
                }
                Product newProduct = new Product(productName, unit, vendor.getId());
                newProduct.setPrice(price);
                newProduct.setBulkThreshold(bulkThreshold);
                newProduct.setBulkPrice(bulkPrice);
                newProduct.setParentProductId(chkSubProduct.isSelected() ? cmbParentProduct.getValue().getId() : null);
                productService.createProduct(newProduct);
            }
            loadProducts();
            clearFormDirect();
            ToastManager.showSuccess(isEditMode ? "Product updated successfully!" : "Product added successfully!");
        } catch (SQLException e) {
            showError("Error saving product: " + e.getMessage());
        } finally {
            btnSave.setDisable(false);
        }
    }

    @FXML
    private void clearForm(javafx.event.ActionEvent event) {
        clearFormDirect();
    }

    @FXML
    private void searchProducts() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        try {
            List<Product> allProducts = productService.getAllProducts();
            productList.clear();
            if (searchText.isEmpty()) {
                productList.addAll(allProducts);
            } else {
                for (Product p : allProducts) {
                    if (p.getProductName().toLowerCase().contains(searchText)) {
                        productList.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error searching: " + e.getMessage());
        }
    }

    public void clearFormDirect() {
        txtProductName.clear();
        txtPrice.clear();
        txtBulkThreshold.clear();
        txtBulkPrice.clear();
        cmbUnit.setButtonCell(new ListCell<String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Unit");
                } else {
                    setText(item);
                }
            }
        });
        cmbUnit.getSelectionModel().clearSelection();
        cmbUnit.setValue(null);
        if (cmbUnit.getEditor() != null) {
            cmbUnit.getEditor().clear();
        }
        cmbUnit.setPromptText("Choose Unit");
        cmbVendor.setButtonCell(new ListCell<Vendor>() {
            protected void updateItem(Vendor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Vendor");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbVendor.getSelectionModel().clearSelection();
        cmbVendor.setValue(null);
        if (cmbVendor.getEditor() != null) {
            cmbVendor.getEditor().clear();
        }
        cmbVendor.setPromptText("Choose Vendor");
        cmbVendor.setDisable(false);
        chkSubProduct.setSelected(false);
        cmbParentProduct.setValue(null);
        parentProductContainer.setVisible(false);
        parentProductContainer.setManaged(false);
        selectedProduct = null;
        isEditMode = false;
        btnSave.setText("Save");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteProduct(Product product) {
        try {
            if (productService.hasChildren(product.getId())) {
                showError("Cannot delete parent product with existing sub products. Remove sub products first.");
                return;
            }
        } catch (SQLException e) {
            showError("Error checking sub products: " + e.getMessage());
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product: " + product.getProductName());
        confirm.setContentText("Are you sure you want to delete this product?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                ToastManager.showSuccess("Product deleted successfully!");
                loadProducts();
            } catch (SQLException e) {
                showError("Cannot delete product: " + e.getMessage());
            }
        }
    }
}
