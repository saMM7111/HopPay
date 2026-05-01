package com.demo.hoppay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Account {
	@Id
	private String accountId;

	@Column(nullable = false)
	private BigDecimal balance;

	@Column(nullable = false)
	private String name;

	protected Account() {
	}

	public Account(String accountId, BigDecimal balance, String name) {
		this.accountId = accountId;
		this.balance = balance;
		this.name = name;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}