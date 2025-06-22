package com.project.q_authent.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomKeyGenerator {

    public static String generateKeyBase64(int bitLength) {
        int byteLength = bitLength / 8;
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String generateKeyHex(int bitLength) {
        byte[] bytes = new byte[bitLength / 8];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes); // 128-bit → 32 hex chars; 256-bit → 64 hex chars
    }
}
