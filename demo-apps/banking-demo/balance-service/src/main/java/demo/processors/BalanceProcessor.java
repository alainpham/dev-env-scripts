package demo.processors;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import demo.entities.AccountBalance;
import demo.model.UpdateBalanceRequest;
import demo.model.UpdateBalanceResponse;

@Component
public class BalanceProcessor {
    
    @Autowired
    private EntityManager entityManager;


    @Transactional
    public UpdateBalanceResponse performUpdate(@Body UpdateBalanceRequest request)
    {
        UpdateBalanceResponse response = new UpdateBalanceResponse();
        AccountBalance balance = entityManager.
            createQuery("select accountBalance from AccountBalance accountBalance where id = :id",AccountBalance.class).
            setParameter("id",request.getId()).
            getSingleResult();

        if (balance.getCurrentBalance().compareTo(request.getAmount()) == -1){
            response.setStatus("failed");
        }else{
            response.setStatus("clearing");
            balance.setCurrentBalance(balance.getCurrentBalance().subtract(request.getAmount()));
            balance.setUpdateDate(new Date());
        }


        response.setLastTransactionAmount(request.getAmount());
        response.setCurrentBalance(balance.getCurrentBalance());
        response.setId(request.getId());
        return response;

    }

    public AccountBalance getBalance(@Header(value = "id") Long id){
        return entityManager.createQuery("select accountBalance from AccountBalance accountBalance where id = :id",AccountBalance.class).setParameter("id",id).getSingleResult();

    }


}
