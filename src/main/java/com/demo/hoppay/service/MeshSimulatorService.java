package com.demo.hoppay.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class MeshSimulatorService {
	private final List<VirtualDevice> devices = new ArrayList<>();
	private final Random random = new Random();

	public void registerDevice(VirtualDevice device) {
		devices.add(device);
	}

	public List<VirtualDevice> getDevices() {
		return Collections.unmodifiableList(devices);
	}

	public void gossipOnce() {
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
}
