package com.universidad.tutorias.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12; // recomendado para GCM

    @Value("${app.crypto.key:}")
    private String base64Key;

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Environment environment;

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(base64Key)) {
            if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
                throw new IllegalStateException("La clave app.crypto.key es obligatoria en producción.");
            } else {
                log.warn("app.crypto.key no está configurada. No se podrá cifrar/descifrar datos sensibles (solo dev).");
                return;
            }
        }
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (decoded.length != 16 && decoded.length != 24 && decoded.length != 32) {
            throw new IllegalStateException("La clave app.crypto.key debe ser base64 de 128/192/256 bits");
        }
        secretKey = new SecretKeySpec(decoded, "AES");
    }

    public boolean isConfigured() {
        return secretKey != null;
    }

    public EncryptionResult encrypt(String plaintext) {
        if (secretKey == null) {
            throw new IllegalStateException("Clave de cifrado no configurada");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptionResult(
                    Base64.getEncoder().encodeToString(cipherText),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Error cifrando información sensible", e);
        }
    }

    public String decrypt(String cipherTextBase64, String ivBase64) {
        if (secretKey == null) {
            throw new IllegalStateException("Clave de cifrado no configurada");
        }
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error descifrando información sensible", e);
        }
    }

    public record EncryptionResult(String cipherText, String iv) {}
}
