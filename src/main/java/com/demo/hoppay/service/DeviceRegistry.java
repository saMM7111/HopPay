package com.demo.hoppay.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceRegistry {
    private final Map<String, VirtualDevice> devices = new ConcurrentHashMap<>();

    public void registerDevice(VirtualDevice device) {
        devices.put(device.getDeviceId(), device);
    }

    public VirtualDevice getDevice(String deviceId) {
        return devices.get(deviceId);
    }

    public List<VirtualDevice> getDevices() {
        return new ArrayList<>(devices.values());
    }
}
