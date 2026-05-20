package com.chickencenter.ui.controllers;

import com.chickencenter.model.Account;
import com.chickencenter.printer.PrinterSettingsManager;
import com.chickencenter.printer.ReceiptPrintTask;
import com.chickencenter.printer.ThermalReceiptBuilder;
import com.chickencenter.service.AccountService;
import com.chickencenter.service.SecurityService;
import com.chickencenter.util.ToastManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import java.sql.SQLException;

public class AccountSettingsController {

    @FXML private Label lblShopName;
    @FXML private Label lblShopAddress;
    @FXML private Label lblContactNo1;
    @FXML private Label lblContactNo2;
    @FXML private Label lblContactNo3;

    @FXML private TextField txtShopName;
    @FXML private TextField txtShopAddress;
    @FXML private TextField txtContactNo1;
    @FXML private TextField txtContactNo2;
    @FXML private TextField txtContactNo3;

    @FXML private PasswordField txtPassword;
    @FXML private Label lblSecurityStatus;
    @FXML private Button btnToggleSecurity;
    @FXML private Button btnSavePassword;
    @FXML private Button btnChangePassword;
    @FXML private Button btnForgotPassword;

    @FXML private Button btnEditShopName;
    @FXML private Button btnEditShopAddress;
    @FXML private Button btnEditContactNo1;
    @FXML private Button btnEditContactNo2;
    @FXML private Button btnEditContactNo3;

    @FXML private ComboBox<String> cmbPrinter;
    @FXML private Button btnRefreshPrinters;
    @FXML private Button btnTestPrint;
    @FXML private Label lblPrinterStatus;

    @FXML private HBox boxShopNameActions;
    @FXML private HBox boxShopAddressActions;
    @FXML private HBox boxContactNo1Actions;
    @FXML private HBox boxContactNo2Actions;
    @FXML private HBox boxContactNo3Actions;

    private final AccountService accountService;
    private final PrinterSettingsManager printerSettingsManager;
    private Account currentAccount;
    private String originalShopName;
    private String originalShopAddress;
    private String originalContactNo1;
    private String originalContactNo2;
    private String originalContactNo3;

    public AccountSettingsController() {
        this.accountService = new AccountService();
        this.printerSettingsManager = new PrinterSettingsManager();
    }

    @FXML
    public void initialize() {
        loadAccount();

        txtShopName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 20) {
                txtShopName.setText(oldVal);
            }
        });

        txtContactNo1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtContactNo1.setText(filtered);
                }
            }
        });

        txtContactNo2.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtContactNo2.setText(filtered);
                }
            }
        });

        txtContactNo3.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtContactNo3.setText(filtered);
                }
            }
        });

        initializePrinterSection();
    }

    private void initializePrinterSection() {
        cmbPrinter.setItems(FXCollections.observableArrayList());
        refreshPrinters();
        cmbPrinter.setOnAction(e -> onPrinterSelected());
    }

    private void loadAccount() {
        try {
            currentAccount = accountService.getAccount();
            if (currentAccount != null) {
                originalShopName = currentAccount.getShopName();
                originalShopAddress = currentAccount.getShopAddress();
                originalContactNo1 = currentAccount.getContactNo1();
                originalContactNo2 = currentAccount.getContactNo2();
                originalContactNo3 = currentAccount.getContactNo3();

                lblShopName.setText(isEmpty(originalShopName) ? "-" : originalShopName);
                lblShopAddress.setText(isEmpty(originalShopAddress) ? "-" : originalShopAddress);
                lblContactNo1.setText(isEmpty(originalContactNo1) ? "-" : originalContactNo1);
                lblContactNo2.setText(isEmpty(originalContactNo2) ? "-" : originalContactNo2);
                lblContactNo3.setText(isEmpty(originalContactNo3) ? "-" : originalContactNo3);

                boolean hasPassword = currentAccount.getPassword() != null && !currentAccount.getPassword().isEmpty();

                if (hasPassword) {
                    txtPassword.setText("********");
                    txtPassword.setDisable(true);
                    btnSavePassword.setVisible(false);
                    btnSavePassword.setManaged(false);
                    btnChangePassword.setVisible(true);
                    btnChangePassword.setManaged(true);
                    btnForgotPassword.setVisible(true);
                    btnForgotPassword.setManaged(true);
                } else {
                    txtPassword.setText("");
                    txtPassword.setDisable(false);
                    btnSavePassword.setVisible(true);
                    btnSavePassword.setManaged(true);
                    btnChangePassword.setVisible(false);
                    btnChangePassword.setManaged(false);
                    btnForgotPassword.setVisible(false);
                    btnForgotPassword.setManaged(false);
                }

                updateSecurityUI(currentAccount.isLocked());
                String savedPrinter = currentAccount.getPrinterName();
                if (savedPrinter != null && !savedPrinter.isEmpty()) {
                    cmbPrinter.getSelectionModel().select(savedPrinter);
                }
            }
        } catch (SQLException e) {
            showError("Error loading account: " + e.getMessage());
        }
    }

    private void updateSecurityUI(boolean isLocked) {
        lblSecurityStatus.setText(isLocked ? "ON" : "OFF");
        lblSecurityStatus.setTextFill(javafx.scene.paint.Paint.valueOf(isLocked ? "#dc2626" : "#6b7280"));
        btnToggleSecurity.setText(isLocked ? "Disable" : "Enable");
        btnToggleSecurity.setStyle(isLocked ?
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 8 12;" :
            "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 8 12;");
    }

    @FXML
    private void savePassword() {
        String password = txtPassword.getText().trim();
        if (password.isEmpty()) {
            showError("Please enter a password");
            return;
        }
        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Password");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to set this password?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                if (currentAccount == null) {
                    currentAccount = new Account();
                }
                currentAccount.setPassword(password);
                currentAccount.setShopName(originalShopName);
                currentAccount.setShopAddress(originalShopAddress);
                currentAccount.setContactNo1(originalContactNo1);
                currentAccount.setContactNo2(originalContactNo2);
                currentAccount.setContactNo3(originalContactNo3);
                currentAccount.setLocked(false);
                accountService.updateAccount(currentAccount);

                loadAccount();
                SecurityService.refreshLockState();
            } catch (SQLException e) {
                showError("Error saving password: " + e.getMessage());
            }
        }
    }

    @FXML
    private void changePassword() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText(null);

        PasswordField oldPwd = new PasswordField();
        oldPwd.setPromptText("Old Password");
        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("New Password");
        PasswordField confirmPwd = new PasswordField();
        confirmPwd.setPromptText("Confirm New Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Old:"), 0, 0);
        grid.add(oldPwd, 1, 0);
        grid.add(new Label("New:"), 0, 1);
        grid.add(newPwd, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPwd, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Pair<>(oldPwd.getText(), newPwd.getText() + "|" + confirmPwd.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String oldPwdVal = result.getKey();
            String newConfirm = result.getValue();
            String[] parts = newConfirm.split("\\|", 2);
            String newPwdVal = parts[0];
            String confirmPwdVal = parts.length > 1 ? parts[1] : "";

            try {
                Account account = accountService.getAccount();
                if (account == null) {
                    showError("Account not found");
                    return;
                }

                String storedPwd = account.getPassword();
                if (storedPwd == null) storedPwd = "";
                storedPwd = storedPwd.trim();
                oldPwdVal = oldPwdVal != null ? oldPwdVal.trim() : "";

                if (storedPwd.isEmpty() || !storedPwd.equals(oldPwdVal)) {
                    showError("Old password is incorrect");
                    return;
                }
                if (newPwdVal.trim().length() < 4) {
                    showError("New password must be at least 4 characters");
                    return;
                }
                if (!newPwdVal.trim().equals(confirmPwdVal.trim())) {
                    showError("New passwords do not match");
                    return;
                }

                account.setPassword(newPwdVal.trim());
                account.setShopName(originalShopName);
                account.setShopAddress(originalShopAddress);
                account.setContactNo1(originalContactNo1);
                account.setContactNo2(originalContactNo2);
                account.setContactNo3(originalContactNo3);
                account.setLocked(currentAccount.isLocked());
                accountService.updateAccount(account);

                loadAccount();
                ToastManager.showSuccess("Password changed successfully!");
            } catch (SQLException e) {
                showError("Error changing password: " + e.getMessage());
            }
        });
    }

    @FXML
    private void forgotPassword() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Reset");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to reset the password?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                Account account = accountService.getAccount();
                if (account == null) {
                    account = new Account();
                }

                String contactNo = account.getContactNo1();
                if (contactNo == null) contactNo = "";
                contactNo = contactNo.trim();

                String newPassword;
                if (contactNo.length() >= 5) {
                    newPassword = contactNo.substring(contactNo.length() - 5);
                } else if (!contactNo.isEmpty()) {
                    newPassword = contactNo;
                } else {
                    newPassword = "12345";
                }

                account.setPassword(newPassword);
                accountService.updateAccount(account);

                loadAccount();
                ToastManager.showSuccess("Password has been reset successfully!");
            } catch (SQLException e) {
                showError("Error resetting password: " + e.getMessage());
            }
        }
    }

    @FXML
    private void toggleSecurity() {
        try {
            if (currentAccount == null) {
                currentAccount = new Account();
            }

            boolean isCurrentlyLocked = currentAccount.isLocked();
            boolean newLockState = !isCurrentlyLocked;

            if (newLockState) {
                Account freshAccount = accountService.getAccount();
                if (freshAccount == null || freshAccount.getPassword() == null || freshAccount.getPassword().trim().isEmpty()) {
                    showError("Please set a password first using Save button");
                    return;
                }

                Alert confirmEnable = new Alert(Alert.AlertType.CONFIRMATION);
                confirmEnable.setTitle("Confirm");
                confirmEnable.setHeaderText(null);
                confirmEnable.setContentText("Are you sure you want to enable security lock? Stock, Purchase, Expense, Employee, and Vendors modules will be protected.");
                if (confirmEnable.showAndWait().get() != ButtonType.OK) {
                    return;
                }

                freshAccount.setLocked(true);
                accountService.updateAccount(freshAccount);

                loadAccount();
                ToastManager.showSuccess("Security lock has been enabled!");
                SecurityService.refreshLockState();
                return;
            } else {
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Authentication Required");
                dialog.setHeaderText(null);
                dialog.setContentText("Enter password to disable security lock");

                PasswordField pwdField = new PasswordField();
                pwdField.setPromptText("Password");
                dialog.getDialogPane().setContent(pwdField);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dialog.setResultConverter(btn -> {
                    if (btn == ButtonType.OK) {
                        return pwdField.getText();
                    }
                    return null;
                });

                String enteredPwd = dialog.showAndWait().orElse(null);
                if (enteredPwd == null || enteredPwd.trim().isEmpty()) {
                    return;
                }

                enteredPwd = enteredPwd.trim();

                Account account = accountService.getAccount();
                if (account == null) {
                    showError("Account not found");
                    return;
                }

                String storedPwd = account.getPassword();
                if (storedPwd == null) storedPwd = "";
                storedPwd = storedPwd.trim();

                if (storedPwd.isEmpty()) {
                    showError("Password not set");
                    return;
                }

                if (!storedPwd.equals(enteredPwd)) {
                    showError("Incorrect password");
                    return;
                }

                Alert confirm2 = new Alert(Alert.AlertType.CONFIRMATION);
                confirm2.setTitle("Confirm");
                confirm2.setHeaderText(null);
                confirm2.setContentText("Are you sure you want to disable security lock?");
                if (confirm2.showAndWait().get() != ButtonType.OK) {
                    return;
                }

                account.setLocked(false);
                accountService.updateAccount(account);

                loadAccount();
                ToastManager.showSuccess("Security lock has been disabled!");
                SecurityService.refreshLockState();
                return;
            }
        } catch (SQLException e) {
            showError("Error updating security: " + e.getMessage());
        }
    }

    private boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }

    private void enterEditMode(TextField tf, Label lbl, HBox actions, Button editBtn, String currentVal) {
        tf.setText(isEmpty(currentVal) ? "" : currentVal);
        tf.setVisible(true);
        tf.setManaged(true);
        tf.setEditable(true);
        tf.requestFocus();

        lbl.setVisible(false);
        lbl.setManaged(false);

        editBtn.setVisible(false);
        editBtn.setManaged(false);

        actions.setVisible(true);
        actions.setManaged(true);
    }

    private void exitEditMode(TextField tf, Label lbl, HBox actions, Button editBtn, String savedVal) {
        tf.setVisible(false);
        tf.setManaged(false);

        lbl.setVisible(true);
        lbl.setManaged(true);
        lbl.setText(isEmpty(savedVal) ? "-" : savedVal);

        editBtn.setVisible(true);
        editBtn.setManaged(true);

        actions.setVisible(false);
        actions.setManaged(false);
    }

    @FXML private void editShopName() {
        enterEditMode(txtShopName, lblShopName, boxShopNameActions, btnEditShopName, originalShopName);
    }

    @FXML private void editShopAddress() {
        enterEditMode(txtShopAddress, lblShopAddress, boxShopAddressActions, btnEditShopAddress, originalShopAddress);
    }

    @FXML private void editContactNo1() {
        enterEditMode(txtContactNo1, lblContactNo1, boxContactNo1Actions, btnEditContactNo1, originalContactNo1);
    }

    @FXML private void editContactNo2() {
        enterEditMode(txtContactNo2, lblContactNo2, boxContactNo2Actions, btnEditContactNo2, originalContactNo2);
    }

    @FXML private void editContactNo3() {
        enterEditMode(txtContactNo3, lblContactNo3, boxContactNo3Actions, btnEditContactNo3, originalContactNo3);
    }

    @FXML private void cancelShopName() {
        exitEditMode(txtShopName, lblShopName, boxShopNameActions, btnEditShopName, originalShopName);
    }

    @FXML private void cancelShopAddress() {
        exitEditMode(txtShopAddress, lblShopAddress, boxShopAddressActions, btnEditShopAddress, originalShopAddress);
    }

    @FXML private void cancelContactNo1() {
        exitEditMode(txtContactNo1, lblContactNo1, boxContactNo1Actions, btnEditContactNo1, originalContactNo1);
    }

    @FXML private void cancelContactNo2() {
        exitEditMode(txtContactNo2, lblContactNo2, boxContactNo2Actions, btnEditContactNo2, originalContactNo2);
    }

    @FXML private void cancelContactNo3() {
        exitEditMode(txtContactNo3, lblContactNo3, boxContactNo3Actions, btnEditContactNo3, originalContactNo3);
    }

    @FXML private void saveShopName() {
        String val = txtShopName.getText().trim();
        if (val.isEmpty()) {
            showError("Shop name is required");
            return;
        }
        if (val.length() > 20) {
            showError("Shop name cannot exceed 20 characters");
            return;
        }
        try {
            currentAccount.setShopName(val);
            accountService.updateAccount(currentAccount);
            originalShopName = val;
            exitEditMode(txtShopName, lblShopName, boxShopNameActions, btnEditShopName, val);
            ToastManager.showSuccess("Updated successfully!");
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML private void saveShopAddress() {
        String val = txtShopAddress.getText().trim();
        if (val.isEmpty()) {
            showError("Shop address is required");
            return;
        }
        try {
            currentAccount.setShopAddress(val);
            accountService.updateAccount(currentAccount);
            originalShopAddress = val;
            exitEditMode(txtShopAddress, lblShopAddress, boxShopAddressActions, btnEditShopAddress, val);
            ToastManager.showSuccess("Updated successfully!");
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML private void saveContactNo1() {
        String val = txtContactNo1.getText().trim();
        if (val.isEmpty()) {
            showError("Contact No 1 is required");
            return;
        }
        try {
            currentAccount.setContactNo1(val);
            accountService.updateAccount(currentAccount);
            originalContactNo1 = val;
            exitEditMode(txtContactNo1, lblContactNo1, boxContactNo1Actions, btnEditContactNo1, val);
            ToastManager.showSuccess("Updated successfully!");
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML private void saveContactNo2() {
        String val = txtContactNo2.getText().trim();
        try {
            currentAccount.setContactNo2(val.isEmpty() ? null : val);
            accountService.updateAccount(currentAccount);
            originalContactNo2 = val;
            exitEditMode(txtContactNo2, lblContactNo2, boxContactNo2Actions, btnEditContactNo2, val);
            ToastManager.showSuccess("Updated successfully!");
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML private void saveContactNo3() {
        String val = txtContactNo3.getText().trim();
        try {
            currentAccount.setContactNo3(val.isEmpty() ? null : val);
            accountService.updateAccount(currentAccount);
            originalContactNo3 = val;
            exitEditMode(txtContactNo3, lblContactNo3, boxContactNo3Actions, btnEditContactNo3, val);
            ToastManager.showSuccess("Updated successfully!");
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void refreshPrinters() {
        String currentSelection = cmbPrinter.getSelectionModel().getSelectedItem();
        cmbPrinter.getItems().clear();
        java.util.List<String> printers = printerSettingsManager.getAvailablePrinters();
        if (printers.isEmpty()) {
            cmbPrinter.getItems().add("No printers found");
            cmbPrinter.getSelectionModel().select(0);
            lblPrinterStatus.setText("No printers detected");
            lblPrinterStatus.setStyle("-fx-text-fill: #dc2626;");
            btnTestPrint.setDisable(true);
            return;
        }
        cmbPrinter.getItems().addAll(printers);
        if (currentSelection != null && printers.contains(currentSelection)) {
            cmbPrinter.getSelectionModel().select(currentSelection);
        } else {
            String saved = printerSettingsManager.getPrinterName();
            if (saved != null && printers.contains(saved)) {
                cmbPrinter.getSelectionModel().select(saved);
            }
        }
        lblPrinterStatus.setText(printers.size() + " printer(s) available");
        lblPrinterStatus.setStyle("-fx-text-fill: #10b981;");
        btnTestPrint.setDisable(false);
    }

    private void onPrinterSelected() {
        String selected = cmbPrinter.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isEmpty() && !"No printers found".equals(selected)) {
            printerSettingsManager.savePrinterName(selected);
            if (currentAccount != null) {
                currentAccount.setPrinterName(selected);
            }
        }
    }

    @FXML
    private void testPrint() {
        String printerName = cmbPrinter.getSelectionModel().getSelectedItem();
        if (printerName == null || printerName.isEmpty() || "No printers found".equals(printerName)) {
            showError("Select a printer first");
            return;
        }
        try {
            String shopName = currentAccount != null ? currentAccount.getShopName() : "JK CHICKEN CENTER";
            ThermalReceiptBuilder builder = new ThermalReceiptBuilder();
            byte[] testData = builder.buildTestReceipt(shopName);
            ReceiptPrintTask task = new ReceiptPrintTask(testData, printerName,
                msg -> Platform.runLater(() -> ToastManager.showSuccess("Test print sent successfully!")),
                err -> Platform.runLater(() -> showError("Test print failed: " + err))
            );
            new Thread(task).start();
        } catch (Exception e) {
            showError("Failed to generate test receipt: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
