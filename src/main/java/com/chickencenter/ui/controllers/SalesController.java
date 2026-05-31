package com.chickencenter.ui.controllers;

import com.chickencenter.model.Product;
import com.chickencenter.model.Sale;
import com.chickencenter.model.SaleItem;
import com.chickencenter.service.BillingService;
import com.chickencenter.service.ProductService;
import com.chickencenter.service.SecurityService;
import com.chickencenter.util.DropdownUtils;
import com.chickencenter.util.TableUtils;
import com.chickencenter.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SalesController {
    @FXML private TableView<Sale> tblSales;
    @FXML private TableColumn<Sale, Integer> colSlNo;
    @FXML private TableColumn<Sale, Integer> colSaleId;
    @FXML private TableColumn<Sale, Double> colAmount;
    @FXML private TableColumn<Sale, String> colPaymentMode;
    @FXML private TableColumn<Sale, Double> colCashAmount;
    @FXML private TableColumn<Sale, Double> colGpayAmount;
    @FXML private TableColumn<Sale, String> colBilled;
    @FXML private TableColumn<Sale, LocalDateTime> colDateTime;
    @FXML private TableColumn<Sale, Void> actionCol;
    @FXML private TableColumn<Sale, Void> deleteCol;
    @FXML private VBox cashCard;
    @FXML private VBox gpayCard;
    @FXML private VBox productSummaryCard;
    @FXML private Button btnDeleteFiltered;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Label lblTotalCash;
    @FXML private Label lblTotalGPay;
    @FXML private FlowPane productSummaryContainer;
    @FXML private VBox saleViewContainer;
    @FXML private VBox itemViewContainer;
    @FXML private Button btnSaleView;
    @FXML private Button btnItemView;
    @FXML private DatePicker itemDpFromDate;
    @FXML private DatePicker itemDpToDate;
    @FXML private ComboBox<Product> cmbItemProduct;
    @FXML private ComboBox<Integer> cmbItemBatch;
    @FXML private TableView<ItemSaleRecord> tblItemSales;
    @FXML private TableColumn<ItemSaleRecord, Integer> colBillNo;
    @FXML private TableColumn<ItemSaleRecord, String> colItemProduct;
    @FXML private TableColumn<ItemSaleRecord, Integer> colItemBatchId;
    @FXML private TableColumn<ItemSaleRecord, Double> colItemQty;
    @FXML private TableColumn<ItemSaleRecord, Double> colItemPrice;
    @FXML private TableColumn<ItemSaleRecord, Double> colItemDiscount;
    @FXML private TableColumn<ItemSaleRecord, Double> colItemAmount;
    @FXML private Label lblTotalItemQty;
    @FXML private Label lblTotalItemDiscount;
    @FXML private Label lblTotalItemAmount;
    @FXML private Label lblItemEmptyState;

    private final String activeTabStyle = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 16;";
    private final String inactiveTabStyle = "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-background-radius: 6; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 16;";

    private final BillingService billingService;
    private final ProductService productService;
    private final SecurityService securityService;
    private final ObservableList<Sale> salesList;
    private final javafx.scene.layout.StackPane cashLockOverlay;
    private final javafx.scene.layout.StackPane gpayLockOverlay;
    private final javafx.scene.layout.StackPane productSummaryLockOverlay;
    private final javafx.scene.layout.StackPane itemViewLockOverlay;
    private final ObservableList<Product> productList;
    private final ObservableList<Product> itemProductList;
    private final ObservableList<ItemSaleRecord> itemSaleList;

    public SalesController() {
        this.billingService = new BillingService();
        this.productService = new ProductService();
        this.securityService = new SecurityService();
        this.salesList = FXCollections.observableArrayList();
        this.productList = FXCollections.observableArrayList();
        this.itemProductList = FXCollections.observableArrayList();
        this.itemSaleList = FXCollections.observableArrayList();
        this.cashLockOverlay = createLockOverlay();
        this.gpayLockOverlay = createLockOverlay();
        this.productSummaryLockOverlay = createLockOverlay();
        this.itemViewLockOverlay = createLockOverlay();
    }

    private StackPane createLockOverlay() {

        StackPane overlay = new StackPane();

        overlay.setPickOnBounds(true);

        overlay.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;"
        );

        // glass effect
        overlay.setEffect(new GaussianBlur(6));

        VBox lockBox = new VBox();
        lockBox.setAlignment(Pos.CENTER);

        Label icon = new Label("🔒");

        icon.setStyle(
                "-fx-font-size: 34px;" +
                        "-fx-text-fill: rgba(255,255,255,0.95);"
        );

        lockBox.getChildren().add(icon);

        overlay.getChildren().add(lockBox);

        StackPane.setAlignment(lockBox, Pos.CENTER);

        overlay.setVisible(false);
        overlay.setManaged(false);

        return overlay;
    }

    private void wrapWithOverlay(VBox card, StackPane overlay) {
        AnchorPane wrapper = new AnchorPane();
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) card.getParent();
        int idx = parent.getChildren().indexOf(card);
        parent.getChildren().remove(card);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);
        wrapper.getChildren().addAll(card, overlay);
        AnchorPane.setTopAnchor(card, 0.0);
        AnchorPane.setBottomAnchor(card, 0.0);
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);
        parent.getChildren().add(idx, wrapper);
        if (parent instanceof HBox) {
            HBox.setHgrow(wrapper, javafx.scene.layout.Priority.ALWAYS);
        }
    }

    private void updateSecurityLockUI() {

        boolean locked = SecurityService.lockEnabledProperty().get();

        GaussianBlur blur = locked ? new GaussianBlur(12) : null;

        cashCard.setEffect(blur);
        gpayCard.setEffect(blur);
        productSummaryCard.setEffect(blur);
        itemViewContainer.setEffect(blur);

        cashLockOverlay.setVisible(locked);
        cashLockOverlay.setManaged(locked);

        gpayLockOverlay.setVisible(locked);
        gpayLockOverlay.setManaged(locked);

        productSummaryLockOverlay.setVisible(locked);
        productSummaryLockOverlay.setManaged(locked);

        itemViewLockOverlay.setVisible(locked);
        itemViewLockOverlay.setManaged(locked);
    }

    public static class ItemSaleRecord {
        private final int billNo;
        private final String productName;
        private final int batchId;
        private final double quantity;
        private final double price;
        private final double discount;
        private final double amount;

        public ItemSaleRecord(int billNo, String productName, int batchId, double quantity, double price, double discount, double amount) {
            this.billNo = billNo;
            this.productName = productName != null ? productName : "";
            this.batchId = batchId;
            this.quantity = quantity;
            this.price = price;
            this.discount = discount;
            this.amount = amount;
        }

        public int getBillNo() { return billNo; }
        public String getProductName() { return productName; }
        public int getBatchId() { return batchId; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getDiscount() { return discount; }
        public double getAmount() { return amount; }
    }

    @FXML
    private void initialize() {
        SecurityService.refreshLockState();
        SecurityService.lockEnabledProperty().addListener((obs, ov, nv) -> {
            Platform.runLater(this::updateSecurityLockUI);
        });

        wrapWithOverlay(cashCard, cashLockOverlay);
        wrapWithOverlay(gpayCard, gpayLockOverlay);
        wrapWithOverlay(productSummaryCard, productSummaryLockOverlay);
        wrapWithOverlay(itemViewContainer, itemViewLockOverlay);

        updateSecurityLockUI();

        tblSales.setItems(null);
        tblSales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        salesList.clear();
        tblSales.setItems(salesList);
        setupTable();
        loadProducts();
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());
        filterSales();
        dpFromDate.setOnAction(e -> filterSales());
        dpToDate.setOnAction(e -> filterSales());

        setupItemTable();
        populateItemProductDropdown();
        DropdownUtils.makeScrollable(cmbItemProduct);
        DropdownUtils.makeScrollable(cmbItemBatch);
        itemDpFromDate.setValue(LocalDate.now());
        itemDpToDate.setValue(LocalDate.now());
        itemDpFromDate.setOnAction(e -> {
            loadItemBatches();
            loadItemSales();
        });
        itemDpToDate.setOnAction(e -> {
            loadItemBatches();
            loadItemSales();
        });
        cmbItemProduct.setOnAction(e -> {
            loadItemBatches();
            loadItemSales();
        });
        cmbItemBatch.setOnAction(e -> loadItemSales());
        loadItemBatches();
        loadItemSales();

        showSaleView();

        Platform.runLater(this::updateSecurityLockUI);
    }

    @FXML
    private void showSaleView() {
        saleViewContainer.setVisible(true);
        saleViewContainer.setManaged(true);
        itemViewContainer.setVisible(false);
        itemViewContainer.setManaged(false);
        javafx.scene.layout.Pane itemWrapper = (javafx.scene.layout.Pane) itemViewContainer.getParent();
        itemWrapper.setVisible(false);
        itemWrapper.setManaged(false);
        btnSaleView.setStyle(activeTabStyle);
        btnItemView.setStyle(inactiveTabStyle);
    }

    @FXML
    private void showItemView() {
        saleViewContainer.setVisible(false);
        saleViewContainer.setManaged(false);
        javafx.scene.layout.Pane itemWrapper = (javafx.scene.layout.Pane) itemViewContainer.getParent();
        itemWrapper.setVisible(true);
        itemWrapper.setManaged(true);
        itemViewContainer.setVisible(true);
        itemViewContainer.setManaged(true);
        btnItemView.setStyle(activeTabStyle);
        btnSaleView.setStyle(inactiveTabStyle);
        loadItemSales();
    }

    public void refreshSales() {
        filterSales();
    }

    private void setupTable() {
        tblSales.setSelectionModel(null);
        tblSales.setMaxHeight(400);
        tblSales.setPrefHeight(400);
        tblSales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colSlNo.setStyle("-fx-alignment: CENTER;");
        colSaleId.setStyle("-fx-alignment: CENTER;");
        colAmount.setStyle("-fx-alignment: CENTER;");
        colPaymentMode.setStyle("-fx-alignment: CENTER;");
        colCashAmount.setStyle("-fx-alignment: CENTER;");
        colGpayAmount.setStyle("-fx-alignment: CENTER;");
        colBilled.setStyle("-fx-alignment: CENTER;");
        colDateTime.setStyle("-fx-alignment: CENTER;");
        actionCol.setStyle("-fx-alignment: CENTER;");

        colSlNo.setCellFactory(col -> new TableCell<Sale, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText(String.valueOf(getIndex() + 1)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colSaleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSaleId.setCellFactory(col -> new TableCell<Sale, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colAmount.setCellFactory(col -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colPaymentMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
        colPaymentMode.setCellFactory(col -> new TableCell<Sale, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    setText(item != null ? item : "Cash");
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 12;");
                }
            }
        });

        colCashAmount.setCellValueFactory(new PropertyValueFactory<>("cashAmount"));
        colCashAmount.setCellFactory(col -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText(String.format("%.2f", item != null ? item : 0)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colGpayAmount.setCellValueFactory(new PropertyValueFactory<>("gpayAmount"));
        colGpayAmount.setCellFactory(col -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText(String.format("%.2f", item != null ? item : 0)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colBilled.setCellValueFactory(cellData -> {
            boolean billed = cellData.getValue().isBilled();
            return new javafx.beans.property.SimpleStringProperty(billed ? "YES" : "NO");
        });
        colBilled.setCellFactory(col -> new TableCell<Sale, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    setText(item);
                    if ("YES".equals(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 12;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 12;");
                    }
                }
            }
        });

        colDateTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDateTime.setCellFactory(col -> new TableCell<Sale, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText(item != null ? item.format(formatter) : ""); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        actionCol.setCellFactory(param -> new TableCell<Sale, Void>() {
            private final StackPane stackPane = new StackPane();
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 12; -fx-cursor: hand;");
                StackPane.setAlignment(viewBtn, Pos.CENTER);
                stackPane.getChildren().add(viewBtn);
                viewBtn.setOnAction(e -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    openSaleDetailsDialog(sale);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : stackPane);
            }
        });

        deleteCol.setCellFactory(param -> new TableCell<Sale, Void>() {
            private final StackPane stackPane = new StackPane();
            private final Button delBtn = new Button("Delete");
            {
                boolean locked = securityService.isDeleteLockEnabled();
                delBtn.setDisable(locked);
                delBtn.setStyle(locked
                    ? "-fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-font-size: 12; -fx-cursor: default;"
                    : "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 12; -fx-cursor: hand;");
                StackPane.setAlignment(delBtn, Pos.CENTER);
                stackPane.getChildren().add(delBtn);
                delBtn.setOnAction(e -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    deleteSale(sale);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : stackPane);
            }
        });

        tblSales.setItems(salesList);
    }

    private void loadProducts() {
        try {
            var products = productService.getAllProducts();
            productList.addAll(products);
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void setupItemTable() {
        tblItemSales.setSelectionModel(null);
        tblItemSales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblItemSales.setPlaceholder(new Label(""));
        tblItemSales.setItems(itemSaleList);
        TableUtils.addSerialNumberColumn(tblItemSales, 0);

        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        colBillNo.setCellFactory(col -> new TableCell<ItemSaleRecord, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemProduct.setCellFactory(col -> new TableCell<ItemSaleRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemBatchId.setCellValueFactory(new PropertyValueFactory<>("batchId"));
        colItemBatchId.setCellFactory(col -> new TableCell<ItemSaleRecord, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemQty.setCellFactory(col -> new TableCell<ItemSaleRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    String display = item % 1 == 0 ? String.valueOf((int) item.doubleValue()) : String.format("%.2f", item);
                    setText(display);
                }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemPrice.setCellFactory(col -> new TableCell<ItemSaleRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colItemDiscount.setCellFactory(col -> new TableCell<ItemSaleRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) { setText(null); }
                else if (item % 1 == 0) { setText("Rs. " + String.valueOf((int) item.doubleValue())); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });

        colItemAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colItemAmount.setCellFactory(col -> new TableCell<ItemSaleRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });
    }

    private void populateItemProductDropdown() {
        try {
            List<Product> allProducts = productService.getAllProducts();
            itemProductList.clear();
            Product allOption = new Product();
            allOption.setId(-1);
            allOption.setProductName("All Products");
            itemProductList.add(allOption);
            for (Product p : allProducts) {
                itemProductList.add(p);
            }
            cmbItemProduct.setItems(itemProductList);
            cmbItemProduct.setCellFactory(lv -> new javafx.scene.control.ListCell<Product>() {
                @Override
                protected void updateItem(Product p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getProductName());
                }
            });
            cmbItemProduct.setButtonCell(new javafx.scene.control.ListCell<Product>() {
                @Override
                protected void updateItem(Product p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getProductName());
                }
            });
            cmbItemProduct.getSelectionModel().select(0);
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void loadItemBatches() {
        LocalDate fromDate = itemDpFromDate.getValue();
        LocalDate toDate = itemDpToDate.getValue();
        if (fromDate == null || toDate == null) {
            cmbItemBatch.setDisable(true);
            cmbItemBatch.setPromptText("No Batch Available");
            cmbItemBatch.setItems(FXCollections.observableArrayList());
            return;
        }
        Product selected = cmbItemProduct.getSelectionModel().getSelectedItem();
        try {
            List<Integer> batches;
            if (selected != null && selected.getId() > 0) {
                batches = billingService.getDistinctBatchesForProduct(selected.getId(), fromDate, toDate);
            } else {
                batches = billingService.getDistinctBatches(fromDate, toDate);
            }
            if (batches.isEmpty()) {
                cmbItemBatch.setDisable(true);
                cmbItemBatch.setPromptText("No Batch Available");
                cmbItemBatch.setItems(FXCollections.observableArrayList());
                cmbItemBatch.getSelectionModel().clearSelection();
                cmbItemBatch.setValue(null);
            } else {
                cmbItemBatch.setDisable(false);
                cmbItemBatch.setPromptText("All Batches");
                ObservableList<Integer> batchOptions = FXCollections.observableArrayList();
                batchOptions.add(-1);
                batchOptions.addAll(batches);
                cmbItemBatch.setItems(batchOptions);
                cmbItemBatch.setCellFactory(lv -> {
                    javafx.scene.control.ListCell<Integer> cell = new javafx.scene.control.ListCell<>() {
                        @Override
                        protected void updateItem(Integer b, boolean empty) {
                            super.updateItem(b, empty);
                            if (empty || b == null) {
                                setText(null);
                                setGraphic(null);
                            } else if (b == -1) {
                                setText("All Batches");
                            } else {
                                setText("Batch " + b);
                            }
                        }
                    };
                    cell.setPrefHeight(36);
                    return cell;
                });
                cmbItemBatch.setButtonCell(new javafx.scene.control.ListCell<Integer>() {
                    @Override
                    protected void updateItem(Integer b, boolean empty) {
                        super.updateItem(b, empty);
                        if (empty || b == null) {
                            setText(null);
                        } else if (b == -1) {
                            setText("All Batches");
                        } else {
                            setText("Batch " + b);
                        }
                    }
                });
                cmbItemBatch.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            cmbItemBatch.setDisable(true);
            cmbItemBatch.setPromptText("No Batch Available");
            cmbItemBatch.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadItemSales() {
        LocalDate fromDate = itemDpFromDate.getValue();
        LocalDate toDate = itemDpToDate.getValue();
        if (fromDate == null || toDate == null) return;

        Product selectedProduct = cmbItemProduct.getSelectionModel().getSelectedItem();
        Integer selectedBatch = cmbItemBatch.getSelectionModel().getSelectedItem();
        if (selectedBatch != null && selectedBatch == -1) selectedBatch = null;

        Integer productId = (selectedProduct != null && selectedProduct.getId() > 0) ? selectedProduct.getId() : null;

        try {
            List<Object[]> rows = billingService.getItemSales(fromDate, toDate, productId, selectedBatch);
            itemSaleList.clear();
            double totalQty = 0;
            double totalDiscount = 0;
            double totalAmount = 0;
            for (Object[] row : rows) {
                int billNo = (int) row[0];
                String prodName = (String) row[1];
                int batchId = row[2] != null ? (int) row[2] : 0;
                double qty = (double) row[3];
                double price = (double) row[4];
                double discount = row[5] != null ? (double) row[5] : 0;
                double amount = (double) row[6];
                itemSaleList.add(new ItemSaleRecord(billNo, prodName, batchId, qty, price, discount, amount));
                totalQty += qty;
                totalDiscount += discount;
                totalAmount += amount;
            }
            boolean hasData = !rows.isEmpty();
            lblItemEmptyState.setVisible(!hasData);
            lblItemEmptyState.setManaged(!hasData);

            tblItemSales.setItems(itemSaleList);
            tblItemSales.refresh();

            String qtyDisplay = totalQty % 1 == 0 ? String.valueOf((int) totalQty) : String.format("%.2f", totalQty);
            lblTotalItemQty.setText(qtyDisplay);
            boolean discountHasDecimals = totalDiscount % 1 != 0;
            lblTotalItemDiscount.setText((discountHasDecimals ? String.format("%.2f", totalDiscount) : String.valueOf((int) totalDiscount)));
            lblTotalItemAmount.setText("Rs. " + String.format("%.2f", totalAmount));
        } catch (SQLException e) {
            showError("Error loading item sales: " + e.getMessage());
        }
    }

    @FXML
    private void filterSales() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        try {
            List<Sale> sales;
            if (fromDate != null && toDate != null) {
                sales = billingService.getFilteredSales(fromDate, toDate);
            } else if (fromDate != null) {
                sales = billingService.getFilteredSales(fromDate, LocalDate.now());
            } else {
                sales = billingService.getAllSales();
            }
            salesList.clear();
            salesList.addAll(sales);
            tblSales.setItems(salesList);
            tblSales.refresh();
            boolean locked = securityService.isDeleteLockEnabled();
            btnDeleteFiltered.setDisable(locked);
            if (locked) {
                btnDeleteFiltered.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #9ca3af; -fx-background-radius: 5; -fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: default;");
            } else {
                btnDeleteFiltered.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: hand;");
            }
            updateSummaries();
            updateSecurityLockUI();
        } catch (SQLException e) {
            showError("Error filtering sales: " + e.getMessage());
        }
    }

    private void updateSummaries() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        if (fromDate == null) fromDate = LocalDate.now();
        if (toDate == null) toDate = LocalDate.now();
        try {
            double totalCash = billingService.getTotalCashByDateRange(fromDate, toDate);
            double totalGPay = billingService.getTotalGPayByDateRange(fromDate, toDate);
            lblTotalCash.setText("Rs. " + String.format("%.2f", totalCash));
            lblTotalGPay.setText("Rs. " + String.format("%.2f", totalGPay));
        } catch (SQLException e) {
            lblTotalCash.setText("Rs. 0.00");
            lblTotalGPay.setText("Rs. 0.00");
        }
        updateProductSummary();
    }

    private void updateProductSummary() {
        if (productSummaryContainer == null) return;
        productSummaryContainer.getChildren().clear();
        
        Map<Integer, Double> soldQtyByProduct = new HashMap<>();
        for (Sale sale : salesList) {
            try {
                List<SaleItem> items = billingService.getSaleItems(sale.getId());
                for (SaleItem item : items) {
                    soldQtyByProduct.merge(item.getItemId(), item.getQuantity(), Double::sum);
                }
            } catch (SQLException e) { /* ignore */ }
        }

        if (soldQtyByProduct.isEmpty()) {
            Label noData = new Label("No product sales data for selected period.");
            noData.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");
            productSummaryContainer.getChildren().add(noData);
            return;
        }

        for (Map.Entry<Integer, Double> entry : soldQtyByProduct.entrySet()) {
            int itemId = entry.getKey();
            double qty = entry.getValue();
            
            String name = "Unknown";
            String unit = "";
            for (Product p : productList) {
                if (p.getId() == itemId) {
                    name = p.getProductName();
                    unit = p.getUnit() != null ? p.getUnit() : "";
                    break;
                }
            }

            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6; -fx-padding: 10; -fx-min-width: 120; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");
            
            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
            
            String qtyDisplay = (qty % 1 == 0) ? String.valueOf((int) qty) : String.format("%.2f", qty);
            Label qtyLabel = new Label(qtyDisplay + (unit.isEmpty() ? "" : " " + unit));
            qtyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2563eb; -fx-font-weight: bold;");
            
            card.getChildren().addAll(nameLabel, qtyLabel);
            productSummaryContainer.getChildren().add(card);
        }
    }

    private void deleteSale(Sale sale) {
        if (sale == null) return;
        if (securityService.isDeleteLockEnabled()) {
            showError("Delete is locked. Disable security lock in Account Settings to proceed.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Sale #" + sale.getId());
        confirm.setContentText("Are you sure you want to delete this sale? Stock will be restored.");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                billingService.deleteSale(sale.getId());
                ToastManager.showSuccess("Sale deleted successfully!");
                filterSales();
            } catch (SQLException e) {
                showError("Error deleting sale: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteFilteredSales() {
        if (securityService.isDeleteLockEnabled()) {
            showError("Delete is locked. Disable security lock in Account Settings to proceed.");
            return;
        }
        if (salesList.isEmpty()) {
            showError("No sales to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Filtered Sales");
        confirm.setContentText("Delete all " + salesList.size() + " filtered sale(s)? Stock will be restored.");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                for (Sale sale : salesList) {
                    billingService.deleteSale(sale.getId());
                }
                ToastManager.showSuccess(salesList.size() + " sale(s) deleted successfully!");
                filterSales();
            } catch (SQLException e) {
                showError("Error deleting sales: " + e.getMessage());
            }
        }
    }

    private void openSaleDetailsDialog(Sale sale) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sale Details");
        dialog.setHeaderText("Sale ID: " + sale.getId());
        TableView<SaleItem> detailTable = new TableView<>();
        detailTable.setPrefWidth(480);
        detailTable.setPrefHeight(300);
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<SaleItem, String> colProduct = new TableColumn<>("Product");
        colProduct.setPrefWidth(120);
        colProduct.setCellValueFactory(cellData -> {
            String name = getProductName(cellData.getValue().getItemId());
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        colProduct.setStyle("-fx-alignment: CENTER;");
        TableColumn<SaleItem, Integer> colBatchId = new TableColumn<>("Batch ID");
        colBatchId.setPrefWidth(70);
        colBatchId.setCellValueFactory(new PropertyValueFactory<>("batchId"));
        colBatchId.setStyle("-fx-alignment: CENTER;");
        TableColumn<SaleItem, Double> colQty = new TableColumn<>("Qty");
        colQty.setPrefWidth(50);
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setStyle("-fx-alignment: CENTER;");
        TableColumn<SaleItem, Double> colPrice = new TableColumn<>("Price");
        colPrice.setPrefWidth(80);
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(col -> new TableCell<SaleItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER-RIGHT;-fx-font-size: 11;");
            }
        });
        TableColumn<SaleItem, Double> colAmountDetail = new TableColumn<>("Amount");
        colAmountDetail.setPrefWidth(100);
        colAmountDetail.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAmountDetail.setCellFactory(col -> new TableCell<SaleItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText("Rs. " + String.format("%.2f", item)); }
                setStyle("-fx-alignment: CENTER-RIGHT;-fx-font-size: 11;");
            }
        });
        detailTable.getColumns().add(TableUtils.createSerialNumberColumn("Sl No", 40));
        detailTable.getColumns().addAll(colProduct, colBatchId, colQty, colPrice, colAmountDetail);
        try {
            List<SaleItem> items = billingService.getSaleItems(sale.getId());
            detailTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            showError("Error loading sale items: " + e.getMessage());
        }
        Label totalLabel = new Label("Total: Rs. " + String.format("%.2f", sale.getTotalAmount()));
        totalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));
        content.getChildren().addAll(detailTable, totalLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getProductName(int productId) {
        for (Product p : productList) {
            if (p.getId() == productId) return p.getProductName();
        }
        return "";
    }

    @FXML
    private void exportToExcel() {
        if (salesList.isEmpty()) {
            showError("No data to export");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        String fileName = "sales_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        fileChooser.setInitialFileName(fileName);
        Stage stage = (Stage) tblSales.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sales Report");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Sl No", "Sale ID", "Amount", "Payment Mode", "Cash", "GPay", "Billed", "Date & Time"};
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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (Sale sale : salesList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue(sale.getId());
                    row.createCell(2).setCellValue(sale.getTotalAmount());
                    row.createCell(3).setCellValue(sale.getPaymentMode() != null ? sale.getPaymentMode() : "Cash");
                    row.createCell(4).setCellValue(sale.getCashAmount());
                    row.createCell(5).setCellValue(sale.getGpayAmount());
                    row.createCell(6).setCellValue(sale.isBilled() ? "YES" : "NO");
                    row.createCell(7).setCellValue(sale.getCreatedAt() != null ? sale.getCreatedAt().format(formatter) : "");
                }
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                ToastManager.showSuccess("Sales report exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error exporting to Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportTodayData() {
        LocalDate today = LocalDate.now();
        try {
            List<Sale> todaySales = billingService.getSalesByDateRange(today, today);
            if (todaySales.isEmpty()) {
                showError("No sales data found for today");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            String fileName = "sales_today_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            fileChooser.setInitialFileName(fileName);
            Stage stage = (Stage) tblSales.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Today's Sales");
                    Row headerRow = sheet.createRow(0);
                    String[] headers = {"Sl No", "Sale ID", "Amount", "Payment Mode", "Cash", "GPay", "Billed", "Date & Time"};
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    for (Sale sale : todaySales) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(rowNum - 1);
                        row.createCell(1).setCellValue(sale.getId());
                        row.createCell(2).setCellValue(sale.getTotalAmount());
                        row.createCell(3).setCellValue(sale.getPaymentMode() != null ? sale.getPaymentMode() : "Cash");
                        row.createCell(4).setCellValue(sale.getCashAmount());
                        row.createCell(5).setCellValue(sale.getGpayAmount());
                        row.createCell(6).setCellValue(sale.isBilled() ? "YES" : "NO");
                        row.createCell(7).setCellValue(sale.getCreatedAt() != null ? sale.getCreatedAt().format(formatter) : "");
                    }
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        workbook.write(fos);
                    }
                    ToastManager.showSuccess("Today's sales exported to:\n" + file.getAbsolutePath());
                } catch (Exception e) {
                    showError("Error exporting to Excel: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            showError("Error loading sales: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
