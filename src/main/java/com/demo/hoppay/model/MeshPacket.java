package com.demo.hoppay.model;

public class MeshPacket {
	private String senderId;
	private int hopCount;
	private int ttl;
	private String signature;
	private String encryptedPayload;

	public MeshPacket() {
	}

	public MeshPacket(String senderId, int hopCount, int ttl, String signature, String encryptedPayload) {
		this.senderId = senderId;
		this.hopCount = hopCount;
		this.ttl = ttl;
		this.signature = signature;
		this.encryptedPayload = encryptedPayload;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getEncryptedPayload() {
		return encryptedPayload;
	}

	public void setEncryptedPayload(String encryptedPayload) {
		this.encryptedPayload = encryptedPayload;
	}
}
