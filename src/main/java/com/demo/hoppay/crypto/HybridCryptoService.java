package com.demo.hoppay.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

@Service
public class HybridCryptoService {
	private static final String AES_ALGORITHM = "AES";
	private static final String AES_TRANSFORM = "AES/GCM/NoPadding";
	private static final int AES_KEY_BITS = 256;
	private static final int GCM_TAG_BITS = 128;
	private static final int GCM_IV_BYTES = 12;
	private static final int RSA_ENCRYPTED_KEY_BYTES = 256;

	private final SecureRandom secureRandom = new SecureRandom();

	public SecretKey generateAesKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
			keyGenerator.init(AES_KEY_BITS, secureRandom);
			return keyGenerator.generateKey();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to generate AES key", ex);
		}
	}

	public AesEncryptedPayload encryptAes(byte[] plaintext, SecretKey key) {
		try {
			byte[] iv = new byte[GCM_IV_BYTES];
			secureRandom.nextBytes(iv);

			Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
			GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, spec);

			byte[] ciphertext = cipher.doFinal(plaintext);
			return new AesEncryptedPayload(iv, ciphertext);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to encrypt payload", ex);
		}
	}

	public byte[] decryptAes(AesEncryptedPayload payload, SecretKey key) {
		try {
			Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
			GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, payload.iv());
			cipher.init(Cipher.DECRYPT_MODE, key, spec);

			return cipher.doFinal(payload.ciphertext());
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to decrypt payload", ex);
		}
	}

	public record AesEncryptedPayload(byte[] iv, byte[] ciphertext) {
	}

	public byte[] encryptAesKey(SecretKey aesKey, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(aesKey.getEncoded());
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to encrypt AES key", ex);
		}
	}

	public byte[] decryptAesKey(byte[] encryptedKey, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(encryptedKey);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to decrypt AES key", ex);
		}
	}

	public byte[] decryptPayload(String base64Payload, PrivateKey privateKey) {
		byte[] all = Base64.getDecoder().decode(base64Payload);
		if (all.length < RSA_ENCRYPTED_KEY_BYTES + GCM_IV_BYTES + (GCM_TAG_BITS / 8)) {
			throw new IllegalArgumentException("Ciphertext too short");
		}

		byte[] encryptedKey = new byte[RSA_ENCRYPTED_KEY_BYTES];
		byte[] iv = new byte[GCM_IV_BYTES];
		byte[] ciphertext = new byte[all.length - RSA_ENCRYPTED_KEY_BYTES - GCM_IV_BYTES];

		ByteBuffer buffer = ByteBuffer.wrap(all);
		buffer.get(encryptedKey);
		buffer.get(iv);
		buffer.get(ciphertext);

		byte[] aesKeyBytes = decryptAesKey(encryptedKey, privateKey);
		SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

		return decryptAes(new AesEncryptedPayload(iv, ciphertext), aesKey);
	}

	public byte[] signPayload(byte[] payload, PrivateKey privateKey) {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(payload);
			return signature.sign();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to sign payload", ex);
		}
	}

	public boolean verifySignature(byte[] payload, byte[] signatureBytes, PublicKey publicKey) {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(publicKey);
			signature.update(payload);
			return signature.verify(signatureBytes);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to verify signature", ex);
		}
	}
}
