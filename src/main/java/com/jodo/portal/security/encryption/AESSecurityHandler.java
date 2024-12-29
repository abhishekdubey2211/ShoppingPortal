package com.jodo.portal.security.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Arrays;

public class AESSecurityHandler {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";
    private static final int AES_256_KEY_LENGTH = 32;

    // Encrypt method
    private byte[] encrypt(String plainText, String secretKey, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
        SecretKeySpec keySpec = new SecretKeySpec(deriveKey(secretKey), AES_ALGORITHM); // Derive key
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    }

    // Decrypt method
    private String decrypt(byte[] encryptedBytes, String secretKey, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
        SecretKeySpec keySpec = new SecretKeySpec(deriveKey(secretKey), AES_ALGORITHM); // Derive key
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // Derive a 256-bit key using SHA-256 to ensure correct length
    private byte[] deriveKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(sha.digest(keyBytes), AES_256_KEY_LENGTH); // Ensure it's 32 bytes (256 bits)
    }

    // Ensure the IV has the desired length (16 bytes for AES/CBC)
    private String ensureIvLength(String iv, int desiredLength) {
        if (iv.length() > desiredLength) {
            return iv.substring(0, desiredLength); // Trim if too long
        } else if (iv.length() < desiredLength) {
            return String.format("%1$-" + desiredLength + "s", iv).replace(' ', '0'); // Pad with '0' if too short
        } else {
            return iv; // Already correct length
        }
    }

    // Public method for encryption
    public String AES_ENCRYPT(String data, String username, String password) throws Exception {
        String iv = ensureIvLength(password, 16);
        byte[] encryptedBytes = encrypt(data, username, iv);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Public method for decryption
    public String AES_DECRYPT(String encryptedData, String username, String password) throws Exception {
        String iv = ensureIvLength(password, 16);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        return decrypt(decodedBytes, username, iv);
    }

//    // Main method for testing
//    public static void main(String[] args) throws Exception {
//        AESSecurityHandler handler = new AESSecurityHandler();
//        String plainText = "Abhishek Dubey"; // Your data to encrypt
//
//        String secretKey = "abhishek@1234"; // Custom key
//        String iv = "Abhi@22112000"; // Must be 16 bytes for AES/CBC
//
//        String encryptedBase64 = handler.AES_ENCRYPT(plainText, secretKey, iv);
//        System.out.println("Encrypted: " + encryptedBase64);
//
//        String decryptedText = handler.AES_DECRYPT(encryptedBase64, secretKey, iv);
//        System.out.println("Decrypted: " + decryptedText);
//    }
}
