package com.demo.hoppay.service;

import com.demo.hoppay.model.MeshPacket;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayDeque;
import java.util.Queue;

public class VirtualDevice {
	private final String deviceId;
	private final boolean hasInternet;
	private final PublicKey publicKey;
	private final PrivateKey privateKey;
	private BigDecimal balance;
	private final Queue<MeshPacket> offlineQueue = new ArrayDeque<>();

	public VirtualDevice(String deviceId,
						 boolean hasInternet,
						 PublicKey publicKey,
						 PrivateKey privateKey,
						 BigDecimal balance) {
		this.deviceId = deviceId;
		this.hasInternet = hasInternet;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.balance = balance;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public boolean hasInternet() {
		return hasInternet;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Queue<MeshPacket> getOfflineQueue() {
		return offlineQueue;
	}
}
