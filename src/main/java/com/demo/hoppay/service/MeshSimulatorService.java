package com.demo.hoppay.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MeshSimulatorService {
	private final Random random = new Random();
	private final BridgeIngestionService bridgeIngestionService;
	private final DeviceRegistry deviceRegistry;

	public MeshSimulatorService(BridgeIngestionService bridgeIngestionService,
						 DeviceRegistry deviceRegistry) {
		this.bridgeIngestionService = bridgeIngestionService;
		this.deviceRegistry = deviceRegistry;
	}

	public void registerDevice(VirtualDevice device) {
		deviceRegistry.registerDevice(device);
	}

	public List<VirtualDevice> getDevices() {
		return deviceRegistry.getDevices();
	}

	public VirtualDevice getDeviceById(String deviceId) {
		return deviceRegistry.getDevice(deviceId);
	}

	public void gossipOnce() {
		List<VirtualDevice> devices = deviceRegistry.getDevices();
		if (devices.size() < 2) {
			return;
		}

		for (VirtualDevice sender : devices) {
			VirtualDevice receiver = devices.get(random.nextInt(devices.size()));
			if (sender == receiver) {
				continue;
			}

			sender.getOfflineQueue().stream().findFirst().ifPresent(packet -> {
				if (packet.getTtl() > 0) {
					packet.setTtl(packet.getTtl() - 1);
					packet.setHopCount(packet.getHopCount() + 1);
					receiver.getOfflineQueue().add(packet);
				}
			});
		}
	}

	public void flushBridges() {
		for (VirtualDevice device : deviceRegistry.getDevices()) {
			if (!device.hasInternet()) {
				continue;
			}

			while (!device.getOfflineQueue().isEmpty()) {
				bridgeIngestionService.ingestPacket(device.getOfflineQueue().poll());
			}
		}
	}
}
