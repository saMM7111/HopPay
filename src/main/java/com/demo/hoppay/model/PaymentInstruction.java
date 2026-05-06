package com.demo.hoppay.model;

import java.math.BigDecimal;

public class PaymentInstruction {
	private String txId;
	private String sender;
	private String receiver;
	private BigDecimal amount;
	private Long signedAt;

	public PaymentInstruction() {
	}

	public PaymentInstruction(String txId, String sender, String receiver, BigDecimal amount, Long signedAt) {
		this.txId = txId;
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
		this.signedAt = signedAt;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
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

	public Long getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(Long signedAt) {
		this.signedAt = signedAt;
	}
}
