package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.PaymentInstruction;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class VirtualDevice {
	private final String deviceId;
	private final boolean hasInternet;
	private final PublicKey publicKey;
	private final PrivateKey privateKey;
	private BigDecimal balance;
	private final Queue<MeshPacket> offlineQueue = new ArrayDeque<>();

	public VirtualDevice(String deviceId,
						 boolean hasInternet,
						 PublicKey publicKey,
						 PrivateKey privateKey,
						 BigDecimal balance) {
		this.deviceId = deviceId;
		this.hasInternet = hasInternet;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.balance = balance;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public boolean hasInternet() {
		return hasInternet;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Queue<MeshPacket> getOfflineQueue() {
		return offlineQueue;
	}

	public MeshPacket createPayment(String receiver, BigDecimal amount, HybridCryptoService cryptoService) {
		PaymentInstruction instruction = new PaymentInstruction(
				UUID.randomUUID().toString(),
				deviceId,
				receiver,
				amount,
				Instant.now().toEpochMilli()
		);

		byte[] payloadBytes = (instruction.getTxId() + "|" + instruction.getSender() + "|" +
				instruction.getReceiver() + "|" + instruction.getAmount() + "|" +
				instruction.getSignedAt()).getBytes(StandardCharsets.UTF_8);

		byte[] signature = cryptoService.signPayload(payloadBytes, privateKey);
		String signatureB64 = Base64.getEncoder().encodeToString(signature);

		String encryptedPayload = Base64.getEncoder().encodeToString(payloadBytes);
		MeshPacket packet = new MeshPacket(0, 5, signatureB64, encryptedPayload);
		offlineQueue.add(packet);
		return packet;
	}
}
