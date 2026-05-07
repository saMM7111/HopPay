package com.demo.hoppay.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MeshSimulatorService {
	private final List<VirtualDevice> devices = new ArrayList<>();

	public void registerDevice(VirtualDevice device) {
		devices.add(device);
	}

	public List<VirtualDevice> getDevices() {
		return Collections.unmodifiableList(devices);
	}
}
