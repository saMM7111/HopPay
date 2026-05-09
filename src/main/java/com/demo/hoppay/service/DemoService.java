package com.demo.hoppay.service;

import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DemoService {
	private final AccountRepository accountRepository;

	public DemoService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
		seedAccounts();
	}

	private void seedAccounts() {
		if (accountRepository.count() > 0) {
			return;
		}

		accountRepository.save(new Account("alice@hoppay", new BigDecimal("2500"), "Alice"));
		accountRepository.save(new Account("bob@hoppay", new BigDecimal("1400"), "Bob"));
		accountRepository.save(new Account("carol@hoppay", new BigDecimal("800"), "Carol"));
		accountRepository.save(new Account("dan@hoppay", new BigDecimal("1800"), "Dan"));
	}
}
