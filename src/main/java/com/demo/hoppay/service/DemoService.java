package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.crypto.ServerKeyHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class DemoService {
	private final DeviceService deviceService;
	private final MeshSimulatorService meshSimulatorService;
	private final HybridCryptoService cryptoService;
	private final ServerKeyHolder serverKeyHolder;
	private final ObjectMapper objectMapper;
	private final Random random = new Random();

	// deviceId -> display name; the last one is the online bridge.
	private final Map<String, String> demoUsers = Map.of(
			"alice@hoppay", "Alice",
			"bob@hoppay", "Bob",
			"carol@hoppay", "Carol",
			"dan@hoppay", "Dan"
	);
	private final List<String> demoOrder = List.of(
			"alice@hoppay", "bob@hoppay", "carol@hoppay", "dan@hoppay");

	public DemoService(DeviceService deviceService,
					   MeshSimulatorService meshSimulatorService,
					   HybridCryptoService cryptoService,
					   ServerKeyHolder serverKeyHolder,
					   ObjectMapper objectMapper) {
		this.deviceService = deviceService;
		this.meshSimulatorService = meshSimulatorService;
		this.cryptoService = cryptoService;
		this.serverKeyHolder = serverKeyHolder;
		this.objectMapper = objectMapper;
		seedDevices();
	}

	private void seedDevices() {
		if (!meshSimulatorService.getDevices().isEmpty()) {
			return;
		}

		BigDecimal[] balances = {
				new BigDecimal("2500"),
				new BigDecimal("1400"),
				new BigDecimal("800"),
				new BigDecimal("1800")
		};
		for (int i = 0; i < demoOrder.size(); i++) {
			String user = demoOrder.get(i);
			boolean hasInternet = "dan@hoppay".equals(user);
			deviceService.createDevice(user, demoUsers.get(user), balances[i], hasInternet);
		}
	}

	@Scheduled(fixedDelayString = "${hoppay.demo.tick-ms:5000}")
	public void generateTraffic() {
		if (meshSimulatorService.getDevices().isEmpty()) {
			return;
		}

		String sender = demoOrder.get(random.nextInt(demoOrder.size()));
		String receiver = demoOrder.stream()
				.filter(user -> !user.equals(sender))
				.findFirst()
				.orElse(sender);

		BigDecimal amount = new BigDecimal(100 + random.nextInt(400));
		VirtualDevice device = meshSimulatorService.getDeviceById(sender);
		if (device != null) {
			device.createPayment(receiver, amount, cryptoService, serverKeyHolder.getPublicKey(), objectMapper);
		}
		meshSimulatorService.gossipOnce();
		meshSimulatorService.flushBridges();
	}
}
