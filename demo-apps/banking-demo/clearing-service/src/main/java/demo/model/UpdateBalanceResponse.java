package demo.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateBalanceResponse implements Serializable{
    
    private Long id;
    private String status;
    private BigDecimal currentBalance;
    private BigDecimal lastTransactionAmount;


    public static UpdateBalanceResponse clear(UpdateBalanceResponse r){
        r.status="success";
        return r;
    }

    public BigDecimal getLastTransactionAmount() {
        return lastTransactionAmount;
    }
    public void setLastTransactionAmount(BigDecimal lastTransactionAmount) {
        this.lastTransactionAmount = lastTransactionAmount;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    
}
