package com.AccountService.AccountService.service;

import brave.Tracer;
import com.AccountService.AccountService.dao.AccountDao;
import com.AccountService.AccountService.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceImplementAccount implements ServiceAccount {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;
    private static String PATTERN_Name = "[a-z]+[^\s]{1,20}";

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {

        List<Account> accountList = (List<Account>) accountDao.findAll();
        List<Account> accountActiveList= new ArrayList<>();

        for(int i=0; i< accountList.size(); i++){

            if(accountList.get(i).getActive()==true){
                accountActiveList.add(accountList.get(i));
            }
        }
        return  accountActiveList;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity finById(Long id) {

          try {
              Account account = accountDao.findById(id).get();
              if(account!=null&&account.getActive()==true){
                  tracer.currentSpan().tag("account find","HttpStatus.ACCEPTED");
                  return   new ResponseEntity( account, HttpStatus.ACCEPTED);
              }
              tracer.currentSpan().tag("Could not find the account","HttpStatus.BAD_REQUEST");
              return new ResponseEntity ("Could not find the account", HttpStatus.BAD_REQUEST);
          }catch (Exception e){
              System.out.println("Exception: "+e);
              tracer.currentSpan().tag("something go bad","HttpStatus.BAD_REQUEST");
              return new ResponseEntity ("something go bad", HttpStatus.BAD_REQUEST);
          }

    }

    @Override
    public ResponseEntity disable(Long id) {
        try {
            Account disableAccount = accountDao.findById(id).get();

            if (disableAccount != null) {
                disableAccount.setActive(false);
                accountDao.save(disableAccount);
                tracer.currentSpan().tag("account disabled","HttpStatus.ACCEPTED");
                return new ResponseEntity( disableAccount, HttpStatus.ACCEPTED);
            }
            tracer.currentSpan().tag("can´t disable the account","HttpStatus.BAD_REQUEST");
            return new ResponseEntity ("can´t disable the account", HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            System.out.println("Exception: "+e);
            tracer.currentSpan().tag("something go bad","HttpStatus.BAD_REQUEST");
            return new ResponseEntity ("something go bad", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity createAccount(Long userId,String alias)  {

        try {
            Boolean existUser = restTemplate.getForObject("http://localhost:8001/user/findActive/"+userId,Boolean.class);

            if(existUser) {
                Boolean talias= alias.matches(PATTERN_Name);
                if(talias) {
                    Account account = new Account(userId, new BigDecimal(0.00), createCbu(9), createAccountNumber(10), alias);
                    accountDao.save(account);
                    tracer.currentSpan().tag("account created","HttpStatus.ACCEPTED");
                    return new ResponseEntity(account, HttpStatus.ACCEPTED);
                }
                tracer.currentSpan().tag("the alias can´t contain spaces","HttpStatus.BAD_REQUEST");
                return new ResponseEntity ("the alias can´t contain spaces", HttpStatus.BAD_REQUEST);
            }
            tracer.currentSpan().tag("user not found","HttpStatus.BAD_REQUEST");
            return new ResponseEntity ("user not found", HttpStatus.BAD_REQUEST);

         }catch (Exception e){
             System.out.println("Exception: "+e);
            tracer.currentSpan().tag("something go bad","HttpStatus.BAD_REQUEST");
            return new ResponseEntity ("something go bad", HttpStatus.BAD_REQUEST);
         }

    }

    private String createCbu(Integer quantity){
        String cbu = "";
        for(int i = 0 ; i < quantity ; i++) {
            cbu += (int) (Math.random() * 9) + 1;
        }

        return cbu;
    }

    private String createAccountNumber(Integer quantity){
        String accountNumber = "";
        for(int i = 0 ; i < quantity ; i++) {
            accountNumber += (int) (Math.random() * 9) ;
        }

        return accountNumber;
    }

    public Boolean findActive(Long id){
               try {
                   Optional<Account> account= accountDao.findById(id);

                   if(account.isPresent() && account.get().getActive()==true){
                       return true;
                   }
                   return false;

               }catch (Exception e){
                   System.out.println("Exception: "+e);
                   return  false;
               }
    }

    public String findAccountNumber(Long id){

        try {
            Optional<Account> account= accountDao.findById(id);

            if(account.isPresent() && account.get().getActive()==true){
                return  account.get().getAccountNumber() ;
            }
            return null;

        }catch (Exception e){
            System.out.println("Exception: "+e);
            return null;
        }
    }

    public Boolean checkTransaction(Long id, Long receiverAccountId , BigDecimal amount){

        Optional<Account> activeAccount = accountDao.findById(id);
        Optional<Account> activeReceiverAccount = accountDao.findById(receiverAccountId);

        try{

            if(activeAccount.isPresent() &&   activeReceiverAccount.isPresent() && activeReceiverAccount.get().getActive()!=false && activeAccount.get().getActive()!=false ){

                Account account = activeAccount.get();
                BigDecimal sum = account.getBalance().add((amount.negate()));
                BigDecimal zero =new BigDecimal(0);

                if(sum.compareTo(zero) >= 0 ) {
                    account.setBalance(sum);
                    activeReceiverAccount.get().setBalance(activeReceiverAccount.get().getBalance().add(amount));

                    accountDao.save(account);
                    accountDao.save(activeReceiverAccount.get());
                    return true;
                }

                throw new RuntimeException("the account does not have enough salary");

            }
            throw new RuntimeException("the account does not exist");

        }catch(Exception e){
            System.out.println("Exception: "+e);
            return false;
        }


}

}
