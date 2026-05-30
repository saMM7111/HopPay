package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.crypto.ServerKeyHolder;
import com.demo.hoppay.model.FlowStep;
import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.PaymentInstruction;
import com.demo.hoppay.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class BridgeIngestionService {
	private final HybridCryptoService cryptoService;
	private final SettlementService settlementService;
	private final ServerKeyHolder serverKeyHolder;
	private final DeviceRegistry deviceRegistry;
	private final ObjectMapper objectMapper;

	public BridgeIngestionService(HybridCryptoService cryptoService,
								  SettlementService settlementService,
								  ServerKeyHolder serverKeyHolder,
								  DeviceRegistry deviceRegistry,
								  ObjectMapper objectMapper) {
		this.cryptoService = cryptoService;
		this.settlementService = settlementService;
		this.serverKeyHolder = serverKeyHolder;
		this.deviceRegistry = deviceRegistry;
		this.objectMapper = objectMapper;
	}

	/**
	 * Decrypts, verifies and settles a packet, throwing on any failure.
	 * Used by the fire-and-forget paths ({@code /api/ingest}, scheduled flush).
	 */
	public void ingestPacket(MeshPacket packet) {
		ingestTraced(packet, new ArrayList<>());
	}

	/**
	 * Same as {@link #ingestPacket} but records each bridge stage into {@code steps}
	 * and returns the settled {@link Transaction} (or {@code null} for an idempotent
	 * duplicate). Used by the dashboard's traced send so the flow can be visualised.
	 */
	public Transaction ingestTraced(MeshPacket packet, List<FlowStep> steps) {
		if (packet.getSignature() == null || packet.getSignature().isBlank()) {
			throw new IllegalArgumentException("Missing signature");
		}

		PublicKey senderKey = resolveSenderKey(packet);

		byte[] payloadBytes = packet.getEncryptedPayload().getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = Base64.getDecoder().decode(packet.getSignature());
		boolean valid = cryptoService.verifySignature(payloadBytes, signatureBytes, senderKey);
		if (!valid) {
			throw new IllegalArgumentException("Invalid signature");
		}
		steps.add(FlowStep.ok("Signature verified",
				"Bridge confirmed packet was signed by " + packet.getSenderId()));

		byte[] plaintext = cryptoService.decryptPayload(
				packet.getEncryptedPayload(),
				serverKeyHolder.getPrivateKey()
		);
		steps.add(FlowStep.ok("Payload decrypted",
				"AES key unwrapped with bridge RSA key; instruction recovered"));

		PaymentInstruction instruction;
		try {
			instruction = objectMapper.readValue(plaintext, PaymentInstruction.class);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to parse payment instruction", ex);
		}

		Transaction transaction = settlementService.processPayment(instruction);
		if (transaction == null) {
			steps.add(FlowStep.info("Idempotency check",
					"Duplicate txId " + instruction.getTxId() + " — already settled, ignored"));
		} else {
			steps.add(FlowStep.ok("Idempotency check",
					"New txId " + instruction.getTxId() + " accepted"));
			steps.add(FlowStep.ok("Settled",
					"Debited " + instruction.getSender() + ", credited " + instruction.getReceiver()
							+ " ₹" + instruction.getAmount()));
		}
		return transaction;
	}

	private PublicKey resolveSenderKey(MeshPacket packet) {
		String senderId = packet.getSenderId();
		if (senderId == null || senderId.isBlank()) {
			throw new IllegalArgumentException("Packet is missing a sender id");
		}
		VirtualDevice sender = deviceRegistry.getDevice(senderId);
		if (sender == null) {
			throw new IllegalArgumentException("Unknown sender device: " + senderId);
		}
		return sender.getPublicKey();
	}
}
