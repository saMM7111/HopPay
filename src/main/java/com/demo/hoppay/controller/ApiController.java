package com.demo.hoppay.controller;

import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.service.BridgeIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
	private final BridgeIngestionService bridgeIngestionService;

	public ApiController(BridgeIngestionService bridgeIngestionService) {
		this.bridgeIngestionService = bridgeIngestionService;
	}

	@PostMapping("/ingest")
	public ResponseEntity<Void> ingest(@RequestBody MeshPacket packet) {
		bridgeIngestionService.ingestPacket(packet);
		return ResponseEntity.accepted().build();
	}
}
