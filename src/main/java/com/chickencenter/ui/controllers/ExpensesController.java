package com.chickencenter.ui.controllers;

import com.chickencenter.model.EmployeeExpense;
import com.chickencenter.model.ShopExpense;
import com.chickencenter.model.VendorExpense;
import com.chickencenter.service.ExpenseService;
import com.chickencenter.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpensesController {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabVendorExpenses;
    @FXML
    private Tab tabEmployeeExpenses;
    @FXML
    private Tab tabShopExpenses;
    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;
    @FXML
    private Button btnExport;
    @FXML
    private TableView<VendorExpense> tblVendorExpenses;
    @FXML
    private TableColumn<VendorExpense, String> colVEVendor;
    @FXML
    private TableColumn<VendorExpense, String> colVENote;
    @FXML
    private TableColumn<VendorExpense, Double> colVEAmount;
    @FXML
    private TableColumn<VendorExpense, String> colVEDate;
    @FXML
    private TableView<EmployeeExpense> tblEmployeeExpenses;
    @FXML
    private TableColumn<EmployeeExpense, String> colEEEmployee;
    @FXML
    private TableColumn<EmployeeExpense, String> colEENote;
    @FXML
    private TableColumn<EmployeeExpense, Double> colEEAmount;
    @FXML
    private TableColumn<EmployeeExpense, String> colEEDate;
    @FXML
    private TableView<ShopExpense> tblShopExpenses;
    @FXML
    private TableColumn<ShopExpense, String> colSENote;
    @FXML
    private TableColumn<ShopExpense, Double> colSEAmount;
    @FXML
    private TableColumn<ShopExpense, String> colSEDate;
    @FXML
    private TableColumn<ShopExpense, Boolean> colSEAction;
    @FXML
    private ComboBox<VendorSelect> cmbExpenseVendor;
    @FXML
    private ComboBox<EmployeeSelect> cmbExpenseEmployee;
    @FXML
    private TextField txtVENote;
    @FXML
    private TextField txtVEAmount;
    @FXML
    private TextField txtEENote;
    @FXML
    private TextField txtEEAmount;
    @FXML
    private DatePicker dpVEDate;
    @FXML
    private DatePicker dpEEDate;
    @FXML
    private TextField txtSENote;
    @FXML
    private TextField txtSEAmount;
    @FXML
    private DatePicker dpSEDate;
    @FXML
    private Button btnAddVendorExpense;
    @FXML
    private Button btnAddEmployeeExpense;
    @FXML
    private Button btnAddShopExpense;
    private final ExpenseService expenseService;
    private final ObservableList<VendorExpense> vendorExpenseList;
    private final ObservableList<EmployeeExpense> employeeExpenseList;
    private final ObservableList<ShopExpense> shopExpenseList;
    private final ObservableList<VendorSelect> vendorList;
    private final ObservableList<EmployeeSelect> employeeList;
    private boolean isShopExpenseEditMode = false;
    private int editingShopExpenseId = 0;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ExpensesController() {
        this.expenseService = new ExpenseService();
        this.vendorExpenseList = FXCollections.observableArrayList();
        this.employeeExpenseList = FXCollections.observableArrayList();
        this.shopExpenseList = FXCollections.observableArrayList();
        this.vendorList = FXCollections.observableArrayList();
        this.employeeList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupVendorExpenseTable();
        setupEmployeeExpenseTable();
        setupShopExpenseTable();
        loadVendors();
        loadEmployees();
        loadVendorExpenses();
        loadEmployeeExpenses();
        loadShopExpenses();
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());
        setupNumericField(txtVEAmount);
        setupNumericField(txtEEAmount);
        setupNumericField(txtSEAmount);
        cmbExpenseVendor.setButtonCell(new ListCell<VendorSelect>() {
            protected void updateItem(VendorSelect item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Vendor");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbExpenseVendor.setPromptText("Select Vendor");
        cmbExpenseEmployee.setButtonCell(new ListCell<EmployeeSelect>() {
            protected void updateItem(EmployeeSelect item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Employee");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbExpenseEmployee.setPromptText("Select Employee");
        dpVEDate.setValue(LocalDate.now());
        dpEEDate.setValue(LocalDate.now());
        dpSEDate.setValue(LocalDate.now());
        dpFromDate.setValue(LocalDate.now().minusMonths(1));
        dpToDate.setValue(LocalDate.now());
        dpFromDate.setOnAction(e -> filterAllExpenses());
        dpToDate.setOnAction(e -> filterAllExpenses());
    }

    private void setupNumericField(TextField field) {
        TextFormatter<Double> formatter = new TextFormatter<>(new DoubleStringConverter(), 0.0, change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        });
        field.setTextFormatter(formatter);
    }

    private void filterAllExpenses() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        if (fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                showError("From date cannot be after To date");
                return;
            }
            try {
                vendorExpenseList.clear();
                vendorExpenseList.addAll(expenseService.getVendorExpensesByDateRange(fromDate, toDate));
                employeeExpenseList.clear();
                employeeExpenseList.addAll(expenseService.getEmployeeExpensesByDateRange(fromDate, toDate));
                shopExpenseList.clear();
                shopExpenseList.addAll(expenseService.getShopExpensesByDateRange(fromDate, toDate));
            } catch (SQLException e) {
                showError("Error filtering expenses: " + e.getMessage());
            }
        }
    }

    private void setupVendorExpenseTable() {
        tblVendorExpenses.setSelectionModel(null);
        tblVendorExpenses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colVEVendor.setCellValueFactory(cellData -> {
            try {
                var vendor = expenseService.getVendor(cellData.getValue().getVendorId());
                return new javafx.beans.property.SimpleStringProperty(vendor != null ? vendor.getName() : "");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        colVENote.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNote() != null ? cellData.getValue().getNote() : ""));
        colVEAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colVEDate.setCellValueFactory(cellData -> {
            String dateStr = "";
            if (cellData.getValue().getExpenseDate() != null) {
                dateStr = cellData.getValue().getExpenseDate().format(dateFormatter);
            }
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        tblVendorExpenses.setItems(vendorExpenseList);
    }

    private void setupEmployeeExpenseTable() {
        tblEmployeeExpenses.setSelectionModel(null);
        tblEmployeeExpenses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colEEEmployee.setCellValueFactory(cellData -> {
            try {
                var emp = expenseService.getEmployee(cellData.getValue().getEmployeeId());
                return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getName() : "");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        colEENote.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNote() != null ? cellData.getValue().getNote() : ""));
        colEEAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colEEDate.setCellValueFactory(cellData -> {
            String dateStr = "";
            if (cellData.getValue().getExpenseDate() != null) {
                dateStr = cellData.getValue().getExpenseDate().format(dateFormatter);
            }
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        tblEmployeeExpenses.setItems(employeeExpenseList);
    }

    private void loadVendors() {
        try {
            vendorList.clear();
            for (var v : expenseService.getAllVendors()) {
                vendorList.add(new VendorSelect(v.getId(), v.getName()));
            }
            cmbExpenseVendor.setItems(vendorList);
        } catch (SQLException e) {
            showError("Error loading vendors");
        }
    }

    private void loadEmployees() {
        try {
            employeeList.clear();
            for (var e : expenseService.getAllEmployees()) {
                employeeList.add(new EmployeeSelect(e.getId(), e.getName()));
            }
            cmbExpenseEmployee.setItems(employeeList);
        } catch (SQLException e) {
            showError("Error loading employees");
        }
    }

    private void loadVendorExpenses() {
        try {
            vendorExpenseList.clear();
            vendorExpenseList.addAll(expenseService.getAllVendorExpenses());
        } catch (SQLException e) {
            showError("Error loading expenses");
        }
    }

    private void loadEmployeeExpenses() {
        try {
            employeeExpenseList.clear();
            employeeExpenseList.addAll(expenseService.getAllEmployeeExpenses());
        } catch (SQLException e) {
            showError("Error loading expenses");
        }
    }

    private void loadShopExpenses() {
        try {
            shopExpenseList.clear();
            shopExpenseList.addAll(expenseService.getAllShopExpenses());
        } catch (SQLException e) {
            showError("Error loading expenses");
        }
    }

    @FXML
    private void addVendorExpense() {
        btnAddVendorExpense.setDisable(true);
        VendorSelect vendor = cmbExpenseVendor.getSelectionModel().getSelectedItem();
        if (vendor == null) {
            showError("Please select a vendor");
            btnAddVendorExpense.setDisable(false);
            return;
        }
        String note = txtVENote.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(txtVEAmount.getText());
        } catch (NumberFormatException e) {
            showError("Invalid amount");
            btnAddVendorExpense.setDisable(false);
            return;
        }
        if (amount <= 0) {
            showError("Amount must be greater than zero");
            btnAddVendorExpense.setDisable(false);
            return;
        }
        LocalDate date = dpVEDate.getValue();
        if (date == null) {
            date = LocalDate.now();
        }
        try {
            VendorExpense expense = new VendorExpense(vendor.getId(), note, amount, date);
            expenseService.createVendorExpense(expense);
            loadVendorExpenses();
            clearVEFields();
        } catch (SQLException e) {
            showError("Error adding expense: " + e.getMessage());
        } finally {
            btnAddVendorExpense.setDisable(false);
        }
    }

    @FXML
    private void addEmployeeExpense() {
        btnAddEmployeeExpense.setDisable(true);
        EmployeeSelect employee = cmbExpenseEmployee.getSelectionModel().getSelectedItem();
        if (employee == null) {
            showError("Please select an employee");
            btnAddEmployeeExpense.setDisable(false);
            return;
        }
        String note = txtEENote.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(txtEEAmount.getText());
        } catch (NumberFormatException e) {
            showError("Invalid amount");
            btnAddEmployeeExpense.setDisable(false);
            return;
        }
        if (amount <= 0) {
            showError("Amount must be greater than zero");
            btnAddEmployeeExpense.setDisable(false);
            return;
        }
        LocalDate date = dpEEDate.getValue();
        if (date == null) {
            date = LocalDate.now();
        }
        try {
            EmployeeExpense expense = new EmployeeExpense(employee.getId(), note, amount, date);
            expenseService.createEmployeeExpense(expense);
            loadEmployeeExpenses();
            clearEEFields();
        } catch (SQLException e) {
            showError("Error adding expense: " + e.getMessage());
        } finally {
            btnAddEmployeeExpense.setDisable(false);
        }
    }

    @FXML
    private void deleteVendorExpense() {
        VendorExpense selected = tblVendorExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an expense to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this expense?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                expenseService.deleteVendorExpense(selected.getId());
                loadVendorExpenses();
            } catch (SQLException e) {
                showError("Error deleting expense");
            }
        }
    }

    @FXML
    private void deleteEmployeeExpense() {
        EmployeeExpense selected = tblEmployeeExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an expense to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this expense?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                expenseService.deleteEmployeeExpense(selected.getId());
                loadEmployeeExpenses();
            } catch (SQLException e) {
                showError("Error deleting expense");
            }
        }
    }

    @FXML
    private void clearVEFields() {
        txtVENote.clear();
        txtVEAmount.clear();
        cmbExpenseVendor.setButtonCell(new ListCell<VendorSelect>() {
            protected void updateItem(VendorSelect item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Vendor");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbExpenseVendor.getSelectionModel().clearSelection();
        cmbExpenseVendor.setValue(null);
        if (cmbExpenseVendor.getEditor() != null) {
            cmbExpenseVendor.getEditor().clear();
        }
        cmbExpenseVendor.setPromptText("Select Vendor");
        dpVEDate.setValue(LocalDate.now());
    }

    @FXML
    private void clearEEFields() {
        txtEENote.clear();
        txtEEAmount.clear();
        cmbExpenseEmployee.setButtonCell(new ListCell<EmployeeSelect>() {
            protected void updateItem(EmployeeSelect item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Employee");
                } else {
                    setText(item.getName());
                }
            }
        });
        cmbExpenseEmployee.getSelectionModel().clearSelection();
        cmbExpenseEmployee.setValue(null);
        if (cmbExpenseEmployee.getEditor() != null) {
            cmbExpenseEmployee.getEditor().clear();
        }
        cmbExpenseEmployee.setPromptText("Select Employee");
        dpEEDate.setValue(LocalDate.now());
    }

    private void setupShopExpenseTable() {
        tblShopExpenses.setSelectionModel(null);
        tblShopExpenses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colSEAction.setStyle("-fx-alignment: CENTER;");
        colSENote.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNote() != null ? cellData.getValue().getNote() : ""));
        colSEAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colSEAmount.setCellFactory(col -> new TableCell<ShopExpense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText("Rs. " + String.format("%.2f", item));
                }
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
            }
        });
        colSEDate.setCellValueFactory(cellData -> {
            String dateStr = "";
            if (cellData.getValue().getExpenseDate() != null) {
                dateStr = cellData.getValue().getExpenseDate().format(dateFormatter);
            }
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        colSEAction.setCellFactory(col -> new TableCell<ShopExpense, Boolean>() {
            private final StackPane stackPane = new StackPane();
            private final Button btnEdit = new Button("Edit");

            {
                btnEdit.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 12; -fx-cursor: hand;");
                StackPane.setAlignment(btnEdit, Pos.CENTER);
                stackPane.getChildren().add(btnEdit);
                btnEdit.setOnAction(e -> {
                    ShopExpense exp = getTableView().getItems().get(getIndex());
                    loadShopExpenseToForm(exp);
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : stackPane);
            }
        });
        tblShopExpenses.setItems(shopExpenseList);
    }

    @FXML
    private void addShopExpense() {
        String note = txtSENote.getText().trim();
        if (note.isEmpty()) {
            showError("Note cannot be empty");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(txtSEAmount.getText());
        } catch (NumberFormatException e) {
            showError("Invalid amount");
            return;
        }
        if (amount <= 0) {
            showError("Amount must be greater than 0");
            return;
        }
        LocalDate date = dpSEDate.getValue();
        if (date == null) {
            date = LocalDate.now();
        }
        try {
            ShopExpense expense = new ShopExpense(note, amount, date);
            if (isShopExpenseEditMode) {
                expense.setId(editingShopExpenseId);
                expenseService.updateShopExpense(expense);
            } else {
                expenseService.createShopExpense(expense);
            }
            loadShopExpenses();
            clearSEFields();
        } catch (SQLException e) {
            showError("Error saving expense: " + e.getMessage());
        }
    }

    @FXML
    private void clearSEFields() {
        txtSENote.clear();
        txtSEAmount.clear();
        dpSEDate.setValue(LocalDate.now());
        isShopExpenseEditMode = false;
        editingShopExpenseId = 0;
        btnAddShopExpense.setText("Add Expense");
    }

    private void loadShopExpenseToForm(ShopExpense exp) {
        txtSENote.setText(exp.getNote());
        txtSEAmount.setText(String.valueOf(exp.getAmount()));
        dpSEDate.setValue(exp.getExpenseDate());
        isShopExpenseEditMode = true;
        editingShopExpenseId = exp.getId();
        btnAddShopExpense.setText("Update Expense");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void exportToExcel() {
        int totalCount = vendorExpenseList.size() + employeeExpenseList.size() + shopExpenseList.size();
        if (totalCount == 0) {
            showError("No expense records found for selected date range");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        String fileName = "expense_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        fileChooser.setInitialFileName(fileName);
        Stage stage = (Stage) btnExport.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Expense Report");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Sl No", "Expense Type", "Name/Note", "Amount", "Date"};
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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (VendorExpense ve : vendorExpenseList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue("Vendor");
                    String vendorName = "";
                    try {
                        vendorName = expenseService.getVendor(ve.getVendorId()).getName();
                    } catch (Exception e) {
                    }
                    row.createCell(2).setCellValue(vendorName + " - " + ve.getNote());
                    row.createCell(3).setCellValue(ve.getAmount());
                    row.createCell(4).setCellValue(ve.getExpenseDate() != null ? ve.getExpenseDate().format(formatter) : "");
                }
                for (EmployeeExpense ee : employeeExpenseList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue("Employee");
                    String empName = "";
                    try {
                        empName = expenseService.getEmployee(ee.getEmployeeId()).getName();
                    } catch (Exception e) {
                    }
                    row.createCell(2).setCellValue(empName + " - " + ee.getNote());
                    row.createCell(3).setCellValue(ee.getAmount());
                    row.createCell(4).setCellValue(ee.getExpenseDate() != null ? ee.getExpenseDate().format(formatter) : "");
                }
                for (ShopExpense se : shopExpenseList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue("Shop");
                    row.createCell(2).setCellValue(se.getNote());
                    row.createCell(3).setCellValue(se.getAmount());
                    row.createCell(4).setCellValue(se.getExpenseDate() != null ? se.getExpenseDate().format(formatter) : "");
                }
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                ToastManager.showSuccess("Expense report exported successfully");
            } catch (Exception e) {
                showError("Error exporting to Excel: " + e.getMessage());
            }
        }
    }

    public static class VendorSelect {
        private final int id;
        private final String name;

        public VendorSelect(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }

    public static class EmployeeSelect {
        private final int id;
        private final String name;

        public EmployeeSelect(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }
}
