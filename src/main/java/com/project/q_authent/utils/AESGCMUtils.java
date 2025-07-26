package com.project.q_authent.utils;

import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AESGCM encode algorithm Utils
 * Last updated at 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@Component
public class AESGCMUtils {

    private final SecretKey secretKey;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public AESGCMUtils(AESKeyProvider keyProvider) {
        this.secretKey = keyProvider.getSecretKey();
    }

    /**
     * Encrypt plain text to encoded text
     * @param plainText {@link String}
     * @return encrypt string
     * @throws Exception NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException
     * @since 1.00
     */
    public String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        ByteBuffer buffer = ByteBuffer.allocate(IV_SIZE + cipherText.length);
        buffer.put(iv);
        buffer.put(cipherText);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Decrypt encoded String to decoded String
     * @param base64CipherText {@link String}
     * @return decoded String
     * @throws Exception err
     * @since 1.00
     */
    public String decrypt(String base64CipherText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64CipherText);

        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[IV_SIZE];
        buffer.get(iv);
        byte[] cipherText = new byte[buffer.remaining()];
        buffer.get(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
