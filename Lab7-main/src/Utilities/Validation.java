package Utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Validation {

    public static boolean validateEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean validateUsername(String username) {
        return username != null &&
                username.length() >= 3 &&
                username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean validateRole(int role) {
        return role == 1 || role == 2;
    }

    public static boolean validateUserId(String userId) {
        return userId != null && !userId.trim().isEmpty();
    }
}