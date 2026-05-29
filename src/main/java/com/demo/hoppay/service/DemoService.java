package com.demo.hoppay.service;

import com.demo.hoppay.crypto.HybridCryptoService;
import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Random;

@Service
public class DemoService {
	private final AccountRepository accountRepository;
	private final MeshSimulatorService meshSimulatorService;
	private final HybridCryptoService cryptoService;
	private final Random random = new Random();
	private final List<String> demoUsers = List.of(
			"alice@hoppay",
			"bob@hoppay",
			"carol@hoppay",
			"dan@hoppay"
	);

	public DemoService(AccountRepository accountRepository,
					   MeshSimulatorService meshSimulatorService,
					   HybridCryptoService cryptoService) {
		this.accountRepository = accountRepository;
		this.meshSimulatorService = meshSimulatorService;
		this.cryptoService = cryptoService;
		seedAccounts();
		seedDevices();
	}

	private void seedAccounts() {
		if (accountRepository.count() > 0) {
			return;
		}

		accountRepository.save(new Account("alice@hoppay", new BigDecimal("2500"), "Alice"));
		accountRepository.save(new Account("bob@hoppay", new BigDecimal("1400"), "Bob"));
		accountRepository.save(new Account("carol@hoppay", new BigDecimal("800"), "Carol"));
		accountRepository.save(new Account("dan@hoppay", new BigDecimal("1800"), "Dan"));
	}

	private void seedDevices() {
		if (!meshSimulatorService.getDevices().isEmpty()) {
			return;
		}

		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialize key generation", ex);
		}

		for (String user : demoUsers) {
			KeyPair keyPair = generator.generateKeyPair();
			boolean hasInternet = "dan@hoppay".equals(user);
			BigDecimal balance = accountRepository.findByAccountId(user)
					.map(Account::getBalance)
					.orElse(BigDecimal.ZERO);

			VirtualDevice device = new VirtualDevice(
					user,
					hasInternet,
					keyPair.getPublic(),
					keyPair.getPrivate(),
					balance
			);
			meshSimulatorService.registerDevice(device);
		}
	}

	@Scheduled(fixedDelayString = "${hoppay.demo.tick-ms:5000}")
	public void generateTraffic() {
		if (meshSimulatorService.getDevices().isEmpty()) {
			return;
		}

		String sender = demoUsers.get(random.nextInt(demoUsers.size()));
		String receiver = demoUsers.stream()
				.filter(user -> !user.equals(sender))
				.findFirst()
				.orElse(sender);

		BigDecimal amount = new BigDecimal(100 + random.nextInt(400));
		meshSimulatorService.getDevices().get(0).createPayment(receiver, amount, cryptoService);
		meshSimulatorService.gossipOnce();
		meshSimulatorService.flushBridges();
	}
}
