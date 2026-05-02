package com.demo.hoppay.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.security.PrivateKey;
import java.security.PublicKey;

public class HybridCryptoService {
	private static final String AES_ALGORITHM = "AES";
	private static final String AES_TRANSFORM = "AES/GCM/NoPadding";
	private static final int AES_KEY_BITS = 256;
	private static final int GCM_TAG_BITS = 128;
	private static final int GCM_IV_BYTES = 12;

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
}
