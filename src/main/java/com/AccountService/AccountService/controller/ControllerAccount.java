package com.AccountService.AccountService.controller;

import com.AccountService.AccountService.entity.Account;
import com.AccountService.AccountService.service.ServiceImplementAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

import java.util.List;

@RestController
@RequestMapping()
public class ControllerAccount {

    @Autowired
    private ServiceImplementAccount serviceAccount;

    @GetMapping("/list")
    public List<Account> list(){

        return serviceAccount.findAll();
    }

    @GetMapping("/list/{id}")
    public ResponseEntity idList(@PathVariable Long id){
        return serviceAccount.finById(id);

    }

    @GetMapping("/find/{id}")
    public ResponseEntity idFind(@PathVariable Long id){
        return serviceAccount.finById(id);

    }

    @GetMapping("/disable/{id}")
    public ResponseEntity disable(@PathVariable Long id){
        return serviceAccount.disable(id);

    }

    @GetMapping("/findActive/{accountId}")
    public Boolean create(@PathVariable("accountId") Long id){
       return serviceAccount.findActive(id);
    }

    @GetMapping("/create/{userId}/{alias}")
    public ResponseEntity create(@PathVariable("userId") Long userId, @PathVariable("alias") String alias){
       return serviceAccount.createAccount(userId,alias);
    }

    @GetMapping("/transaction/{id}/{receiverAccountId}/{amount}")
    public Boolean create(@PathVariable("id") Long id, @PathVariable("receiverAccountId") Long receiverAccountId , @PathVariable("amount") String amount){
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        return serviceAccount.checkTransaction( id, receiverAccountId , amountBigDecimal);
    }
    @GetMapping("/findAccountNumber/{accountId}")
    public String findAccountNumber(@PathVariable("accountId") Long id){
        return serviceAccount.findAccountNumber(id);
    }


}
