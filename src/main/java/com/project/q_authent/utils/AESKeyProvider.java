package com.project.q_authent.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Getter
@Component
public class AESKeyProvider {
    private final SecretKey secretKey;

    public AESKeyProvider(@Value("${custom.aes-key}") String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(decoded, 0, decoded.length, "AES");
    }
}