package com.chronicorn.frontend.utils;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SecurityUtils {
    private static final String SECRET_SALT = "finpro_gacha_secret_key_2026";

    public static String generateVerificationKey(String userId, int amount) {
        try {
            String input = userId + ":" + amount + ":" + SECRET_SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating verification key", e);
        }
    }
}
