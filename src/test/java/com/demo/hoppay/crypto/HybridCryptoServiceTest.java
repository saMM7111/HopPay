package com.demo.hoppay.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridCryptoServiceTest {
    @Test
    void aesRoundTripWorks() {
        HybridCryptoService service = new HybridCryptoService();
        SecretKey key = service.generateAesKey();
        byte[] plaintext = "hello-hoppay".getBytes(StandardCharsets.UTF_8);

        HybridCryptoService.AesEncryptedPayload payload = service.encryptAes(plaintext, key);
        byte[] decrypted = service.decryptAes(payload, key);

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void rsaWrapAndSignatureWork() throws Exception {
        HybridCryptoService service = new HybridCryptoService();
        SecretKey key = service.generateAesKey();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        byte[] wrapped = service.encryptAesKey(key, keyPair.getPublic());
        byte[] unwrapped = service.decryptAesKey(wrapped, keyPair.getPrivate());
        assertArrayEquals(key.getEncoded(), unwrapped);

        byte[] payload = "sign-me".getBytes(StandardCharsets.UTF_8);
        byte[] signature = service.signPayload(payload, keyPair.getPrivate());
        assertTrue(service.verifySignature(payload, signature, keyPair.getPublic()));
    }
}
