package com.project.q_authent.utils;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class RSAKeyUtils {
    public static String genPublicKey(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
        BigInteger modulus = rsaPrivateKey.getModulus();
        BigInteger publicExponent = rsaPrivateKey.getPublicExponent();

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String generateRsaPrivateKeyBase64(int keySize) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize); // 2048 hoặc 4096
        KeyPair keyPair = keyGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        byte[] encodedPrivateKey = privateKey.getEncoded(); // PKCS#8 format

        return Base64.getEncoder().encodeToString(encodedPrivateKey);
    }
}
