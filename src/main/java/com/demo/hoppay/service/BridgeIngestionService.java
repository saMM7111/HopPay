package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.model.MeshPacket;
import org.springframework.stereotype.Service;

@Service
public class BridgeIngestionService {
	private final HybridCryptoService cryptoService;
	private final SettlementService settlementService;

	public BridgeIngestionService(HybridCryptoService cryptoService, SettlementService settlementService) {
		this.cryptoService = cryptoService;
		this.settlementService = settlementService;
	}

	public void ingestPacket(MeshPacket packet) {
	}
}
