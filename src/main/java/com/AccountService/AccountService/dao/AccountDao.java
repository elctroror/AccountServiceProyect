package com.AccountService.AccountService.dao;

import com.AccountService.AccountService.entity.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountDao extends CrudRepository<Account, Long> {
}
