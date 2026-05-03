package com.demo.hoppay.service;

import com.demo.hoppay.model.AccountRepository;
import com.demo.hoppay.model.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public SettlementService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}
}
