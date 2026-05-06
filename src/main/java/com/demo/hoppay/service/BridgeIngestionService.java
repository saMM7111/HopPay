package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.crypto.ServerKeyHolder;
import com.demo.hoppay.model.MeshPacket;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class BridgeIngestionService {
	private final HybridCryptoService cryptoService;
	private final SettlementService settlementService;
	private final ServerKeyHolder serverKeyHolder;

	public BridgeIngestionService(HybridCryptoService cryptoService,
								  SettlementService settlementService,
								  ServerKeyHolder serverKeyHolder) {
		this.cryptoService = cryptoService;
		this.settlementService = settlementService;
		this.serverKeyHolder = serverKeyHolder;
	}

	public void ingestPacket(MeshPacket packet) {
		if (packet.getSignature() == null || packet.getSignature().isBlank()) {
			throw new IllegalArgumentException("Missing signature");
		}

		byte[] payloadBytes = packet.getEncryptedPayload().getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = Base64.getDecoder().decode(packet.getSignature());
		boolean valid = cryptoService.verifySignature(payloadBytes, signatureBytes, serverKeyHolder.getPublicKey());
		if (!valid) {
			throw new IllegalArgumentException("Invalid signature");
		}

		cryptoService.decryptPayload(packet.getEncryptedPayload(), serverKeyHolder.getPrivateKey());
	}
}
