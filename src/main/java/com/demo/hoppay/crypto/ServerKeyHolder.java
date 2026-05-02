package com.demo.hoppay.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class ServerKeyHolder {
	private static final int RSA_KEY_SIZE = 2048;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	@PostConstruct
	public void generateKeys() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(RSA_KEY_SIZE);
			KeyPair keyPair = generator.generateKeyPair();
			this.privateKey = keyPair.getPrivate();
			this.publicKey = keyPair.getPublic();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to generate RSA key pair", ex);
		}
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}
}
