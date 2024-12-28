package com.example.demo.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {

    public static String generateSecretKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public static void main(String[] args) {
        System.out.println("Сгенерированный секретный ключ: " + generateSecretKey());
    }
}
