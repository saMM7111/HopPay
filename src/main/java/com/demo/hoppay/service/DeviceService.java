package com.demo.hoppay.service;

import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * Creates and registers mesh devices together with their backing UPI account.
 * Shared by the demo seeding and the dashboard's "Add Device" endpoint so the
 * keypair + account + registry wiring lives in exactly one place.
 */
@Service
public class DeviceService {
	private static final int RSA_KEY_SIZE = 2048;

	private final AccountRepository accountRepository;
	private final MeshSimulatorService meshSimulatorService;
	private final KeyPairGenerator keyPairGenerator;

	public DeviceService(AccountRepository accountRepository,
						 MeshSimulatorService meshSimulatorService) {
		this.accountRepository = accountRepository;
		this.meshSimulatorService = meshSimulatorService;
		try {
			this.keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			this.keyPairGenerator.initialize(RSA_KEY_SIZE);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialize key generation", ex);
		}
	}

	/**
	 * Generates an RSA keypair, persists a funded {@link Account} and registers a
	 * {@link VirtualDevice} on the mesh. Throws if a device with this id already exists.
	 */
	public synchronized VirtualDevice createDevice(String deviceId,
												   String name,
												   BigDecimal balance,
												   boolean hasInternet) {
		if (deviceId == null || deviceId.isBlank()) {
			throw new IllegalArgumentException("Device id is required");
		}
		if (meshSimulatorService.getDeviceById(deviceId) != null) {
			throw new IllegalArgumentException("Device already exists: " + deviceId);
		}
		BigDecimal startingBalance = balance == null ? BigDecimal.ZERO : balance;
		if (startingBalance.signum() < 0) {
			throw new IllegalArgumentException("Balance cannot be negative");
		}

		String accountName = (name == null || name.isBlank()) ? deviceId : name;
		accountRepository.save(new Account(deviceId, startingBalance, accountName));

		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		VirtualDevice device = new VirtualDevice(
				deviceId,
				hasInternet,
				keyPair.getPublic(),
				keyPair.getPrivate(),
				startingBalance
		);
		meshSimulatorService.registerDevice(device);
		return device;
	}
}
