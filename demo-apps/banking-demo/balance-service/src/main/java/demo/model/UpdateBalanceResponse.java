package demo.model;

import demo.entities.AccountBalance;

public class UpdateBalanceResponse {
    
    private Boolean succeded;
    private AccountBalance balance;
    public Boolean getSucceded() {
        return succeded;
    }
    public void setSucceded(Boolean succeded) {
        this.succeded = succeded;
    }
    public AccountBalance getBalance() {
        return balance;
    }
    public void setBalance(AccountBalance balance) {
        this.balance = balance;
    }

    
}
