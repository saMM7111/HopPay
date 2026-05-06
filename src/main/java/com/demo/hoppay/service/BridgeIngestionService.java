package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.crypto.ServerKeyHolder;
import com.demo.hoppay.model.MeshPacket;
import org.springframework.stereotype.Service;

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
		cryptoService.decryptPayload(packet.getEncryptedPayload(), serverKeyHolder.getPrivateKey());
	}
}
