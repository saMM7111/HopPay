package com.demo.hoppay.controller;

import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.Transaction;
import com.demo.hoppay.model.TransactionRepository;
import com.demo.hoppay.service.BridgeIngestionService;
import com.demo.hoppay.service.DeviceService;
import com.demo.hoppay.service.MeshSimulatorService;
import com.demo.hoppay.service.PaymentFlowService;
import com.demo.hoppay.service.VirtualDevice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
	private final BridgeIngestionService bridgeIngestionService;
	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final MeshSimulatorService meshSimulatorService;
	private final DeviceService deviceService;
	private final PaymentFlowService paymentFlowService;

	public ApiController(BridgeIngestionService bridgeIngestionService,
						 TransactionRepository transactionRepository,
						 AccountRepository accountRepository,
						 MeshSimulatorService meshSimulatorService,
						 DeviceService deviceService,
						 PaymentFlowService paymentFlowService) {
		this.bridgeIngestionService = bridgeIngestionService;
		this.transactionRepository = transactionRepository;
		this.accountRepository = accountRepository;
		this.meshSimulatorService = meshSimulatorService;
		this.deviceService = deviceService;
		this.paymentFlowService = paymentFlowService;
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

	@GetMapping("/devices")
	public List<DeviceResponse> devices() {
		return meshSimulatorService.getDevices().stream()
				.map(this::toDeviceResponse)
				.sorted(Comparator.comparing(DeviceResponse::deviceId))
				.toList();
	}

	@PostMapping("/devices")
	public ResponseEntity<?> createDevice(@RequestBody CreateDeviceRequest request) {
		try {
			VirtualDevice device = deviceService.createDevice(
					request.deviceId(), request.name(), request.balance(), request.hasInternet());
			return ResponseEntity.status(HttpStatus.CREATED).body(toDeviceResponse(device));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
		}
	}

	@PostMapping("/payments")
	public ResponseEntity<PaymentFlowService.FlowResult> sendPayment(@RequestBody PaymentRequest request) {
		PaymentFlowService.FlowResult result =
				paymentFlowService.sendTraced(request.senderId(), request.receiverId(), request.amount());
		return ResponseEntity.ok(result);
	}

	@GetMapping("/transactions")
	public TransactionsResponse transactions() {
		List<TransactionRow> rows = transactionRepository.findAll().stream()
				.sorted(Comparator.comparing(Transaction::getTxId).reversed())
				.limit(50)
				.map(tx -> new TransactionRow(
						tx.getTxId(), tx.getSender(), tx.getReceiver(),
						tx.getAmount(), tx.getStatus(), tx.getTimestamp().toString()))
				.toList();
		BigDecimal volume = transactionRepository.totalVolume();
		return new TransactionsResponse(transactionRepository.count(), volume, rows);
	}

	private DeviceResponse toDeviceResponse(VirtualDevice device) {
		Account account = accountRepository.findByAccountId(device.getDeviceId()).orElse(null);
		BigDecimal balance = account != null ? account.getBalance() : device.getBalance();
		String name = account != null ? account.getName() : device.getDeviceId();
		return new DeviceResponse(device.getDeviceId(), name, balance,
				device.hasInternet(), device.getOfflineQueue().size());
	}

	private StatusResponse toStatusResponse(Transaction transaction) {
		return new StatusResponse(transaction.getTxId(), transaction.getStatus());
	}

	public record StatusResponse(Long txId, String status) {
	}

	public record DeviceResponse(String deviceId, String name, BigDecimal balance,
								 boolean hasInternet, int queueSize) {
	}

	public record CreateDeviceRequest(String deviceId, String name, BigDecimal balance, boolean hasInternet) {
	}

	public record PaymentRequest(String senderId, String receiverId, BigDecimal amount) {
	}

	public record TransactionRow(Long txId, String sender, String receiver,
								 BigDecimal amount, String status, String timestamp) {
	}

	public record TransactionsResponse(long totalTransactions, BigDecimal totalVolume,
									   List<TransactionRow> transactions) {
	}

	public record ErrorResponse(String error) {
	}
}
