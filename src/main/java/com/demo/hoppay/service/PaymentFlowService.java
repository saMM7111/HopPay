package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.crypto.ServerKeyHolder;
import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import com.demo.hoppay.model.FlowStep;
import com.demo.hoppay.model.MeshPacket;
import com.demo.hoppay.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates a single payment so its whole journey across the mesh can be
 * visualised. Unlike the random gossip in {@link MeshSimulatorService}, this
 * walks a deterministic path from the sender to the nearest bridge so a manual
 * send always reaches settlement and produces a complete step-by-step trace.
 */
@Service
public class PaymentFlowService {
	private static final int MAX_RELAYS = 2;

	private final DeviceRegistry deviceRegistry;
	private final BridgeIngestionService bridgeIngestionService;
	private final HybridCryptoService cryptoService;
	private final ServerKeyHolder serverKeyHolder;
	private final AccountRepository accountRepository;
	private final ObjectMapper objectMapper;

	public PaymentFlowService(DeviceRegistry deviceRegistry,
							  BridgeIngestionService bridgeIngestionService,
							  HybridCryptoService cryptoService,
							  ServerKeyHolder serverKeyHolder,
							  AccountRepository accountRepository,
							  ObjectMapper objectMapper) {
		this.deviceRegistry = deviceRegistry;
		this.bridgeIngestionService = bridgeIngestionService;
		this.cryptoService = cryptoService;
		this.serverKeyHolder = serverKeyHolder;
		this.accountRepository = accountRepository;
		this.objectMapper = objectMapper;
	}

	public FlowResult sendTraced(String senderId, String receiverId, BigDecimal amount) {
		List<FlowStep> steps = new ArrayList<>();

		VirtualDevice sender = deviceRegistry.getDevice(senderId);
		if (sender == null) {
			return failed(steps, senderId, receiverId, amount, "Unknown sender device: " + senderId);
		}
		if (receiverId == null || deviceRegistry.getDevice(receiverId) == null) {
			return failed(steps, senderId, receiverId, amount, "Unknown receiver device: " + receiverId);
		}
		if (senderId.equals(receiverId)) {
			return failed(steps, senderId, receiverId, amount, "Sender and receiver must differ");
		}
		if (amount == null || amount.signum() <= 0) {
			return failed(steps, senderId, receiverId, amount, "Amount must be positive");
		}

		// 1. Offline creation: encrypt + sign on the device, no internet involved.
		MeshPacket packet = sender.createPayment(
				receiverId, amount, cryptoService, serverKeyHolder.getPublicKey(), objectMapper);
		sender.getOfflineQueue().remove(packet);
		steps.add(FlowStep.ok("Created offline on " + senderId,
				"AES-256 encrypted, RSA-wrapped key, SHA-256withRSA signed — no internet"));

		// 2. Hop across the mesh to the nearest bridge.
		List<VirtualDevice> path = buildPathToBridge(sender);
		if (path == null) {
			return failed(steps, senderId, receiverId, amount,
					"No internet-connected bridge reachable on the mesh");
		}

		VirtualDevice current = sender;
		for (VirtualDevice next : path) {
			packet.setTtl(packet.getTtl() - 1);
			packet.setHopCount(packet.getHopCount() + 1);
			next.getOfflineQueue().add(packet);
			steps.add(FlowStep.info("Mesh hop " + packet.getHopCount(),
					current.getDeviceId() + " → " + next.getDeviceId() + " (ttl " + packet.getTtl() + ")"));
			current = next;
		}
		current.getOfflineQueue().remove(packet);
		steps.add(FlowStep.ok("Reached bridge " + current.getDeviceId(),
				"Bridge is online — forwarding to settlement"));

		// 3. Bridge ingestion: verify, decrypt, idempotency, settle.
		Transaction transaction;
		try {
			transaction = bridgeIngestionService.ingestTraced(packet, steps);
		} catch (Exception ex) {
			return failed(steps, senderId, receiverId, amount, rootMessage(ex));
		}

		BigDecimal senderBalance = balanceOf(senderId);
		BigDecimal receiverBalance = balanceOf(receiverId);

		if (transaction == null) {
			return new FlowResult(steps, "DUPLICATE", null,
					senderId, receiverId, amount, senderBalance, receiverBalance);
		}

		steps.add(FlowStep.ok("Balances updated",
				senderId + ": ₹" + senderBalance + "  •  " + receiverId + ": ₹" + receiverBalance));
		return new FlowResult(steps, "SETTLED", transaction.getTxId(),
				senderId, receiverId, amount, senderBalance, receiverBalance);
	}

	/**
	 * Deterministic route: up to {@value #MAX_RELAYS} relay devices followed by the
	 * first bridge (a device with internet). Returns an empty path when the sender
	 * is itself a bridge, or {@code null} when no bridge exists.
	 */
	private List<VirtualDevice> buildPathToBridge(VirtualDevice sender) {
		if (sender.hasInternet()) {
			return List.of();
		}

		VirtualDevice bridge = null;
		List<VirtualDevice> relays = new ArrayList<>();
		for (VirtualDevice device : deviceRegistry.getDevices()) {
			if (device == sender) {
				continue;
			}
			if (device.hasInternet()) {
				bridge = device;
			} else if (relays.size() < MAX_RELAYS) {
				relays.add(device);
			}
		}

		if (bridge == null) {
			return null;
		}
		relays.add(bridge);
		return relays;
	}

	private BigDecimal balanceOf(String accountId) {
		return accountRepository.findByAccountId(accountId)
				.map(Account::getBalance)
				.orElse(BigDecimal.ZERO);
	}

	private FlowResult failed(List<FlowStep> steps, String senderId, String receiverId,
							  BigDecimal amount, String reason) {
		steps.add(FlowStep.fail("Failed", reason));
		return new FlowResult(steps, "FAILED", null, senderId, receiverId, amount,
				balanceOf(senderId), balanceOf(receiverId));
	}

	private String rootMessage(Throwable ex) {
		Throwable cause = ex;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		return cause.getMessage() == null ? ex.toString() : cause.getMessage();
	}

	public record FlowResult(List<FlowStep> steps,
							 String status,
							 Long txId,
							 String sender,
							 String receiver,
							 BigDecimal amount,
							 BigDecimal senderBalance,
							 BigDecimal receiverBalance) {
	}
}
