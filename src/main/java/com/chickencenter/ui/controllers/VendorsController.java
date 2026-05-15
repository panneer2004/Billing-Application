package com.chickencenter.ui.controllers;

import com.chickencenter.model.Vendor;
import com.chickencenter.service.ExpenseService;
import com.chickencenter.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class VendorsController {
    @FXML
    private TableView<Vendor> tblVendors;
    @FXML
    private TableColumn<Vendor, String> colName;
    @FXML
    private TableColumn<Vendor, String> colContact;
    @FXML
    private TableColumn<Vendor, Boolean> colAction;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtContact;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnClear;
    private final ExpenseService expenseService;
    private final ObservableList<Vendor> vendorList;
    private Vendor selectedVendor;
    private boolean isEditMode = false;

    public VendorsController() {
        this.expenseService = new ExpenseService();
        this.vendorList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadVendors();
        setupAlphabetField(txtName);
        tblVendors.setRowFactory(tv -> {
            TableRow<Vendor> row = new TableRow<>();
            row.setPrefHeight(44);
            return row;
        });
    }

    private void setupAlphabetField(TextField field) {
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("[a-zA-Z ]*")) {
                return change;
            }
            return null;
        });
        field.setTextFormatter(formatter);
    }

    private void setupTable() {
        tblVendors.setSelectionModel(null);
        tblVendors.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        tblVendors.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAction.setCellFactory(col -> new TableCell<Vendor, Boolean>() {
            private final HBox hbox = new HBox();
            private final Button btnEdit = new Button("Edit");

            {
                btnEdit.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-cursor: hand;");
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().add(btnEdit);
                btnEdit.setOnAction(e -> {
                    Vendor vendor = getTableView().getItems().get(getIndex());
                    loadVendorToForm(vendor);
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
        tblVendors.setItems(vendorList);
    }

    private void loadVendorToForm(Vendor vendor) {
        selectedVendor = vendor;
        isEditMode = true;
        txtName.setText(vendor.getName());
        txtContact.setText(vendor.getContactNumber());
        btnSave.setText("Update");
    }

    private void loadVendors() {
        try {
            vendorList.clear();
            vendorList.addAll(expenseService.getAllVendors());
        } catch (SQLException e) {
            showError("Error loading vendors: " + e.getMessage());
        }
    }

    @FXML
    private void searchVendors() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        try {
            List<Vendor> allVendors = expenseService.getAllVendors();
            vendorList.clear();
            if (searchText.isEmpty()) {
                vendorList.addAll(allVendors);
            } else {
                for (Vendor v : allVendors) {
                    if (v.getName().toLowerCase().contains(searchText) || (v.getContactNumber() != null && v.getContactNumber().contains(searchText))) {
                        vendorList.add(v);
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error searching vendors: " + e.getMessage());
        }
    }

    @FXML
    private void saveVendor() {
        btnSave.setDisable(true);
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter vendor name");
            btnSave.setDisable(false);
            return;
        }
        try {
            List<Vendor> allVendors = expenseService.getAllVendors();
            for (Vendor v : allVendors) {
                if (v.getName().equalsIgnoreCase(name)) {
                    if (selectedVendor == null || selectedVendor.getId() != v.getId()) {
                        showError("Vendor name already exists! Please use a different name.");
                        btnSave.setDisable(false);
                        return;
                    }
                }
            }
            if (selectedVendor != null && isEditMode) {
                selectedVendor.setName(name);
                selectedVendor.setContactNumber(contact);
                expenseService.updateVendor(selectedVendor);
            } else {
                Vendor vendor = new Vendor(name, contact);
                expenseService.createVendor(vendor);
            }
            loadVendors();
            clearFormDirect();
            ToastManager.showSuccess("Vendor saved successfully!");
        } catch (SQLException e) {
            showError("Error saving vendor: " + e.getMessage());
        } finally {
            btnSave.setDisable(false);
        }
    }

    public void clearFormDirect() {
        txtName.clear();
        txtContact.clear();
        selectedVendor = null;
        isEditMode = false;
        btnSave.setText("Save");
        if (tblVendors.getSelectionModel() != null) {
            tblVendors.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void clearFormClick() {
        clearFormDirect();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
