package demo.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateBalanceRequest implements Serializable{
    
    private Long id;
    private BigDecimal amount;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    } 

    
}
