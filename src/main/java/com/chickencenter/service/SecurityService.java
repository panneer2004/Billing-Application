package com.chickencenter.service;

import com.chickencenter.model.Account;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecurityService {

    private static final BooleanProperty lockEnabled = new SimpleBooleanProperty(false);

    private static final Set<String> PROTECTED_MODULES = new HashSet<>(
        Arrays.asList("purchase", "employees", "vendors")
    );

    private final AccountService accountService;

    public static BooleanProperty lockEnabledProperty() {
        return lockEnabled;
    }

    public static void refreshLockState() {
        try {
            AccountService svc = new AccountService();
            Account account = svc.getAccount();
            lockEnabled.set(account != null && account.isLocked());
        } catch (SQLException e) {
            e.printStackTrace();
            lockEnabled.set(false);
        }
    }

    public SecurityService() {
        this.accountService = new AccountService();
    }

    public boolean isSecurityEnabled() throws SQLException {
        Account account = accountService.getAccount();
        return account != null && account.isLocked();
    }

    public boolean isDeleteLockEnabled() {
        try {
            return isSecurityEnabled();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkDeleteAccess(String feature) {
        try {
            if (!isSecurityEnabled()) {
                return true;
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Action Locked");
            alert.setHeaderText(null);
            alert.setContentText(capitalize(feature) + " deletion is locked. Disable security lock in Account Settings to proceed.");
            alert.showAndWait();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkSecurityAccess(String moduleName) {
        if (!PROTECTED_MODULES.contains(moduleName)) {
            return true;
        }

        try {
            if (!isSecurityEnabled()) {
                return true;
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.setContentText(capitalize(moduleName) + " module is locked. Disable security lock in Account Settings to access.");
            alert.showAndWait();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
