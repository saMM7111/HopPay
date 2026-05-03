package com.demo.hoppay.service;

import com.demo.hoppay.model.AccountRepository;
import com.demo.hoppay.model.TransactionRepository;
import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.PaymentInstruction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettlementService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public SettlementService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	public void processPayment(PaymentInstruction instruction) {
		Account sender = accountRepository.findByAccountId(instruction.getSender())
				.orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

		accountRepository.findByAccountId(instruction.getReceiver())
				.orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

		BigDecimal amount = instruction.getAmount();
		if (amount == null || amount.signum() <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}

		if (sender.getBalance().compareTo(amount) < 0) {
			throw new IllegalStateException("Insufficient funds");
		}
	}
}
