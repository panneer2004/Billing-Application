package com.chickencenter.ui.controllers;

import com.chickencenter.dao.PurchaseDAO;
import com.chickencenter.service.ProductService;
import com.chickencenter.util.DropdownUtils;
import com.chickencenter.util.TableUtils;
import com.chickencenter.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseController {

    @FXML private ComboBox<PurchaseDAO.ProductWithVendor> cmbProduct;
    @FXML private TextField txtVendorName;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtRate;
    @FXML private Label lblTotalAmount;
    @FXML private Button btnPurchase;

    @FXML private TableView<PurchaseDAO.PurchaseWithDetails> tblPurchases;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, String> colProductName;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, String> colVendorName;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Integer> colBatchId;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Double> colQuantity;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Double> colBalanceQty;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Double> colRate;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Double> colTotalAmount;
    @FXML private TableColumn<PurchaseDAO.PurchaseWithDetails, Boolean> colAction;

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Button btnExport;

    private final ProductService productService;
    private final PurchaseDAO purchaseDAO;
    private final ObservableList<PurchaseDAO.ProductWithVendor> productList;
    private final ObservableList<PurchaseDAO.PurchaseWithDetails> purchaseList;

    private PurchaseDAO.PurchaseWithDetails selectedPurchase;
    private boolean isEditMode = false;

    public PurchaseController() {
        this.productService = new ProductService();
        this.purchaseDAO = new PurchaseDAO();
        this.productList = FXCollections.observableArrayList();
        this.purchaseList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();
        loadPurchases();

        setupNumericField(txtQuantity);
        setupNumericField(txtRate);

        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        txtRate.textProperty().addListener((obs, oldVal, newVal) -> calculateTotal());

        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());

        filterPurchases();

        dpFromDate.setOnAction(e -> filterPurchases());
        dpToDate.setOnAction(e -> filterPurchases());

        cmbProduct.setButtonCell(new ListCell<PurchaseDAO.ProductWithVendor>() {
            protected void updateItem(PurchaseDAO.ProductWithVendor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Product");
                } else {
                    setText(item.getProductName());
                }
            }
        });
        cmbProduct.setPromptText("Choose Product");

        cmbProduct.setOnAction(e -> loadProductDetails());
        DropdownUtils.makeScrollable(cmbProduct);
    }

    private void setupNumericField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*(\\.\\d{0,2})?")) {
                field.setText(oldVal);
            }
        });
    }

    private void calculateTotal() {
        try {
            double qty = txtQuantity.getText().isEmpty() ? 0 : Double.parseDouble(txtQuantity.getText());
            double rate = txtRate.getText().isEmpty() ? 0 : Double.parseDouble(txtRate.getText());
            double total = qty * rate;
            lblTotalAmount.setText(String.format("Rs. %.2f", total));
        } catch (NumberFormatException e) {
            lblTotalAmount.setText("Rs. 0.00");
        }
    }

    private void setupTable() {
        tblPurchases.setSelectionModel(null);
        tblPurchases.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableUtils.addSerialNumberColumn(tblPurchases, 0);

        colProductName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));

        colVendorName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVendorName()));

        colBatchId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getItemBatchId()).asObject());

        colQuantity.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getBatchQuantity()).asObject());

        colBalanceQty.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getBalanceQuantity()).asObject());
        colBalanceQty.setCellFactory(col -> new TableCell<PurchaseDAO.PurchaseWithDetails, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item % 1 == 0 ? String.valueOf(item.intValue()) : String.format("%.2f", item));
                    setAlignment(Pos.CENTER);
                    if (item == 0) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item <= 20) {
                        setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colRate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRate()).asObject());

        colTotalAmount.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        colAction.setCellFactory(col -> new TableCell<PurchaseDAO.PurchaseWithDetails, Boolean>() {
            private final HBox hbox = new HBox();
            private final Button btnEdit = new Button("Edit");

            {
                btnEdit.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-cursor: hand;");
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().add(btnEdit);

                btnEdit.setOnAction(e -> {
                    PurchaseDAO.PurchaseWithDetails p = getTableView().getItems().get(getIndex());
                    loadPurchaseToForm(p);
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        tblPurchases.setItems(purchaseList);
    }

    private void loadProducts() {
        try {
            productList.clear();
            List<PurchaseDAO.ProductWithVendor> products = productService.getAllProductsWithVendor();
            productList.addAll(products);
            cmbProduct.setItems(productList);

            if (products.isEmpty()) {
                cmbProduct.setPromptText("No products - add in Products tab");
            } else {
                cmbProduct.setPromptText("Choose Product");
            }
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void loadPurchases() {
        try {
            purchaseList.clear();
            purchaseList.addAll(productService.getAllPurchaseDetails());
        } catch (SQLException e) {
            showError("Error loading purchases: " + e.getMessage());
        }
    }

    private void filterPurchases() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        if (fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                showError("From date cannot be after To date");
                return;
            }

            try {
                purchaseList.clear();
                purchaseList.addAll(productService.getPurchaseDetailsByDateRange(fromDate, toDate));
            } catch (SQLException e) {
                showError("Error filtering purchases: " + e.getMessage());
            }
        } else {
            loadPurchases();
        }
    }

    private void loadProductDetails() {
        PurchaseDAO.ProductWithVendor product = cmbProduct.getSelectionModel().getSelectedItem();
        if (product != null) {
            txtVendorName.setText(product.getVendorName() != null ? product.getVendorName() : "N/A");
            txtQuantity.clear();
            txtRate.clear();
            lblTotalAmount.setText("Rs. 0.00");
        } else {
            txtVendorName.clear();
            txtQuantity.clear();
            txtRate.clear();
            lblTotalAmount.setText("Rs. 0.00");
        }
    }

    private void loadPurchaseToForm(PurchaseDAO.PurchaseWithDetails p) {
        selectedPurchase = p;
        isEditMode = true;

        for (PurchaseDAO.ProductWithVendor prod : productList) {
            if (prod.getId() == p.getItemId()) {
                cmbProduct.setValue(prod);
                break;
            }
        }

        txtVendorName.setText(p.getVendorName());
        txtQuantity.setText(String.valueOf(p.getBatchQuantity()));
        txtRate.setText(String.valueOf(p.getRate()));
        calculateTotal();

        System.out.println("[Edit Purchase] Product: " + p.getProductName() + " | Qty: " + p.getBatchQuantity() + " | Rate: " + p.getRate());

        cmbProduct.setDisable(true);
        btnPurchase.setText("Update");
    }

    @FXML
    private void makePurchase() {
        btnPurchase.setDisable(true);

        PurchaseDAO.ProductWithVendor selectedProduct = cmbProduct.getSelectionModel().getSelectedItem();

        if (selectedProduct == null && !isEditMode) {
            showError("Please select a product");
            btnPurchase.setDisable(false);
            return;
        }

        double quantity, rate, totalAmount;
        try {
            quantity = Double.parseDouble(txtQuantity.getText());
            rate = Double.parseDouble(txtRate.getText());

            if (quantity <= 0) {
                showError("Please enter valid quantity (greater than 0)");
                btnPurchase.setDisable(false);
                return;
            }
            if (rate <= 0) {
                showError("Please enter valid rate (greater than 0)");
                btnPurchase.setDisable(false);
                return;
            }

            totalAmount = quantity * rate;
        } catch (NumberFormatException e) {
            showError("Invalid input - please enter valid numbers");
            btnPurchase.setDisable(false);
            return;
        }

        try {
            if (isEditMode && selectedPurchase != null) {
                productService.updatePurchase(selectedPurchase.getId(), quantity, rate, totalAmount);
            } else {
                productService.makePurchase(selectedProduct.getId(), quantity, rate, totalAmount);
            }

            clearFormDirect();
            loadPurchases();
            loadProducts();
            ToastManager.showSuccess(isEditMode ? "Purchase updated successfully!" : "Purchase added successfully!");
        } catch (SQLException e) {
            showError("Error making purchase: " + e.getMessage());
        } finally {
            btnPurchase.setDisable(false);
        }
    }

    public void clearFormDirect() {
        cmbProduct.setButtonCell(new ListCell<PurchaseDAO.ProductWithVendor>() {
            protected void updateItem(PurchaseDAO.ProductWithVendor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose Product");
                } else {
                    setText(item.getProductName());
                }
            }
        });

        cmbProduct.getSelectionModel().clearSelection();
        cmbProduct.setValue(null);
        if (cmbProduct.getEditor() != null) {
            cmbProduct.getEditor().clear();
        }
        cmbProduct.setPromptText("Choose Product");

        cmbProduct.setDisable(false);
        txtVendorName.clear();
        txtQuantity.clear();
        txtRate.clear();
        lblTotalAmount.setText("Rs. 0.00");
        selectedPurchase = null;
        isEditMode = false;
        btnPurchase.setText("Make Purchase");
    }

    @FXML
    public void clearFields() {
        clearFormDirect();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void exportToExcel() {
        if (purchaseList.isEmpty()) {
            showError("No purchase records found for selected date range");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        String fileName = "purchase_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        fileChooser.setInitialFileName(fileName);

        Stage stage = (Stage) tblPurchases.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Purchase Report");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"Sl No", "Product Name", "Vendor", "Batch ID", "Quantity", "Balance Qty", "Rate", "Amount"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                int rowNum = 1;
                for (PurchaseDAO.PurchaseWithDetails p : purchaseList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue(p.getProductName());
                    row.createCell(2).setCellValue(p.getVendorName());
                    row.createCell(3).setCellValue(p.getItemBatchId());
                    row.createCell(4).setCellValue(p.getBatchQuantity());
                    row.createCell(5).setCellValue(p.getBalanceQuantity());
                    row.createCell(6).setCellValue(p.getRate());
                    row.createCell(7).setCellValue(p.getTotalAmount());
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                ToastManager.showSuccess("Purchase report exported to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                showError("Error exporting to Excel: " + e.getMessage());
            }
        }
    }
}
