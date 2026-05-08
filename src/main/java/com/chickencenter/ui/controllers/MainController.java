package com.chickencenter.ui.controllers;

import com.chickencenter.service.BillingService;
import com.chickencenter.service.ExpenseService;
import com.chickencenter.service.AccountService;
import com.chickencenter.service.SecurityService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnBilling;
    @FXML private Button btnSales;
    @FXML private Button btnProducts;
    @FXML private Button btnStock;
    @FXML private Button btnPurchase;
    @FXML private Button btnVendors;
    @FXML private Button btnEmployees;
    @FXML private Button btnExpenses;
    @FXML private Button btnAccountSettings;

    private final String activeBtnStyle = "-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-background-radius: 14; -fx-cursor: hand; -fx-padding: 0 20;";
    private final String normalBtnStyle = "-fx-background-color: transparent; -fx-background-radius: 14; -fx-cursor: hand; -fx-padding: 0 20;";

    private final BillingService billingService;
    private final ExpenseService expenseService;
    private final AccountService accountService;
    private final SecurityService securityService;

    public MainController() {
        this.billingService = new BillingService();
        this.expenseService = new ExpenseService();
        this.accountService = new AccountService();
        this.securityService = new SecurityService();
    }

    @FXML
    public void initialize() {
        NavigationHelper.setNavigationCallback(() -> {
            String target = NavigationHelper.getNavigateTo();
            if (target != null) {
                switch (target) {
                    case "billing": showBilling(); break;
                    case "products": showProducts(); break;
                    case "sales": showSales(); break;
                    case "stock": showStock(); break;
                    case "purchase": showPurchase(); break;
                    case "vendors": showVendors(); break;
                    case "employees": showEmployees(); break;
                    case "expenses": showExpenses(); break;
                    case "dashboard": showDashboard(); break;
                    case "account-settings": showAccountSettings(); break;
                    default: showDashboard(); break;
                }
                NavigationHelper.clearNavigateTo();
            }
        });
        showDashboard();
    }

    public void refreshDashboardData() {
        showDashboard();
    }

    private void resetButtonStyles() {
        btnDashboard.setStyle(normalBtnStyle);
        btnBilling.setStyle(normalBtnStyle);
        btnSales.setStyle(normalBtnStyle);
        btnProducts.setStyle(normalBtnStyle);
        btnStock.setStyle(normalBtnStyle);
        btnPurchase.setStyle(normalBtnStyle);
        btnVendors.setStyle(normalBtnStyle);
        btnEmployees.setStyle(normalBtnStyle);
        btnExpenses.setStyle(normalBtnStyle);
        btnAccountSettings.setStyle(normalBtnStyle);
    }

    private void loadView(String fxmlPath, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newView = loader.load();

            newView.setOpacity(0.0);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newView);

            resetButtonStyles();
            if (activeBtn != null) activeBtn.setStyle(activeBtnStyle);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load view");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadViewAndRefresh(String fxmlPath, Button activeBtn, String refreshMethod) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newView = loader.load();

            newView.setOpacity(0.0);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newView);

            resetButtonStyles();
            if (activeBtn != null) activeBtn.setStyle(activeBtnStyle);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            if ("refreshDashboard".equals(refreshMethod)) {
                DashboardController controller = loader.getController();
                if (controller != null) {
                    controller.refreshDashboard();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath + " - " + e.getMessage());
        }
    }

    @FXML
    private void showDashboard() {
        loadViewAndRefresh("/com/chickencenter/ui/views/dashboard-view.fxml", btnDashboard, "refreshDashboard");
    }

    @FXML
    private void showBilling() {
        loadView("/com/chickencenter/ui/billing.fxml", btnBilling);
    }

    @FXML
    private void showSales() {
        loadView("/com/chickencenter/ui/views/sales-view.fxml", btnSales);
    }

    @FXML
    private void showProducts() {
        loadView("/com/chickencenter/ui/views/products-view.fxml", btnProducts);
    }

    @FXML
    private void showStock() {
        if (!securityService.checkSecurityAccess("stock")) return;
        loadView("/com/chickencenter/ui/views/stock-view.fxml", btnStock);
    }

    @FXML
    private void showPurchase() {
        if (!securityService.checkSecurityAccess("purchase")) return;
        loadView("/com/chickencenter/ui/views/purchase-view.fxml", btnPurchase);
    }

    @FXML
    private void showVendors() {
        if (!securityService.checkSecurityAccess("vendors")) return;
        loadView("/com/chickencenter/ui/views/vendors-view.fxml", btnVendors);
    }

    @FXML
    private void showEmployees() {
        if (!securityService.checkSecurityAccess("employees")) return;
        loadView("/com/chickencenter/ui/views/employees-view.fxml", btnEmployees);
    }

    @FXML
    private void showExpenses() {
        if (!securityService.checkSecurityAccess("expenses")) return;
        loadView("/com/chickencenter/ui/views/expenses-view.fxml", btnExpenses);
    }

    @FXML
    private void showAccountSettings() {
        loadView("/com/chickencenter/ui/views/account-settings-view.fxml", btnAccountSettings);
    }
}
