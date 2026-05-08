package com.demo.hoppay.controller;

import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.Transaction;
import com.demo.hoppay.model.TransactionRepository;
import com.demo.hoppay.service.BridgeIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
	private final BridgeIngestionService bridgeIngestionService;
	private final TransactionRepository transactionRepository;

	public ApiController(BridgeIngestionService bridgeIngestionService,
						 TransactionRepository transactionRepository) {
		this.bridgeIngestionService = bridgeIngestionService;
		this.transactionRepository = transactionRepository;
	}

	@PostMapping("/ingest")
	public ResponseEntity<Void> ingest(@RequestBody MeshPacket packet) {
		bridgeIngestionService.ingestPacket(packet);
		return ResponseEntity.accepted().build();
	}

	@GetMapping("/status/{txId}")
	public ResponseEntity<StatusResponse> status(@PathVariable Long txId) {
		return transactionRepository.findById(txId)
				.map(transaction -> ResponseEntity.ok(toStatusResponse(transaction)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private StatusResponse toStatusResponse(Transaction transaction) {
		return new StatusResponse(transaction.getTxId(), transaction.getStatus());
	}

	public record StatusResponse(Long txId, String status) {
	}
}
