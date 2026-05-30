package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.PaymentInstruction;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
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

	/**
	 * Builds a real hybrid-encrypted, signed mesh packet offline:
	 * the PaymentInstruction JSON is AES-256 encrypted, the AES key is RSA-wrapped
	 * with the bridge's public key, and the resulting payload is signed with this
	 * device's private key. The on-wire layout (RSA key ‖ IV ‖ ciphertext) matches
	 * {@link HybridCryptoService#decryptPayload}.
	 */
	public MeshPacket createPayment(String receiver,
									BigDecimal amount,
									HybridCryptoService cryptoService,
									PublicKey bridgePublicKey,
									ObjectMapper objectMapper) {
		PaymentInstruction instruction = new PaymentInstruction(
				UUID.randomUUID().toString(),
				deviceId,
				receiver,
				amount,
				Instant.now().toEpochMilli()
		);

		try {
			byte[] plaintext = objectMapper.writeValueAsBytes(instruction);

			SecretKey aesKey = cryptoService.generateAesKey();
			HybridCryptoService.AesEncryptedPayload encrypted = cryptoService.encryptAes(plaintext, aesKey);
			byte[] encryptedKey = cryptoService.encryptAesKey(aesKey, bridgePublicKey);

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			buffer.write(encryptedKey);
			buffer.write(encrypted.iv());
			buffer.write(encrypted.ciphertext());
			String encryptedPayload = Base64.getEncoder().encodeToString(buffer.toByteArray());

			byte[] signature = cryptoService.signPayload(
					encryptedPayload.getBytes(StandardCharsets.UTF_8), privateKey);
			String signatureB64 = Base64.getEncoder().encodeToString(signature);

			MeshPacket packet = new MeshPacket(deviceId, 0, 5, signatureB64, encryptedPayload);
			offlineQueue.add(packet);
			return packet;
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to assemble payment packet", ex);
		}
	}
}
