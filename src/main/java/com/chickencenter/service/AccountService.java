package com.chickencenter.service;

import com.chickencenter.dao.AccountDAO;
import com.chickencenter.model.Account;
import java.sql.SQLException;

public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
    }

    public Account getAccount() throws SQLException {
        return accountDAO.getAccount();
    }

    public void updateAccount(Account account) throws SQLException {
        accountDAO.updateAccount(account);
    }
}