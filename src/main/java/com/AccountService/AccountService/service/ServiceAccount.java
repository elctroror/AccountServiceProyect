package com.AccountService.AccountService.service;

import com.AccountService.AccountService.entity.Account;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ServiceAccount {

    public List<Account> findAll();
    public ResponseEntity finById(Long id);
    public ResponseEntity disable(Long id);
    public ResponseEntity createAccount(Long userId, String alias);

}
