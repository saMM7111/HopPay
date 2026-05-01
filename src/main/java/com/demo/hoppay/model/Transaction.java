package com.demo.hoppay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long txId;

	@Column(nullable = false)
	private String sender;

	@Column(nullable = false)
	private String receiver;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private Instant timestamp;

	protected Transaction() {
	}

	public Transaction(String sender, String receiver, BigDecimal amount, String status, Instant timestamp) {
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
		this.status = status;
		this.timestamp = timestamp;
	}

	public Long getTxId() {
		return txId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
}
