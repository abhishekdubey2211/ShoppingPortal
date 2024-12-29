package com.jodo.portal.security.encryption;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKey;

public class Main {

    private static Cipher getCipher(boolean encrypting, String password, String salt) throws Exception {
        byte[] iv = new byte[16];
        SecretKeySpec keySpec = generateKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(encrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher;
    }

    // Derive AES key using PBKDF2 and SHA-1
    private static SecretKeySpec generateKey(String password, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    // Encrypt the raw string and encode it in Base64
    public static String encryptAndEncode(String raw, String password, String salt) throws Exception {
        Cipher cipher = getCipher(true, password, salt);
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = cipher.doFinal(bytes);
        return Base64.getEncoder().encodeToString(encryptedBytes); // Return Base64 encoded string
    }

    // Decrypt the Base64 encoded string
    public static String decryptAndDecode(String encryptedText, String password, String salt) throws Exception {
        Cipher cipher = getCipher(false, password, salt);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8); // Return decrypted string
    }

//    // Example usage
//    public static void main(String[] args) {
//        try {
//            String plainString = "9769078266";
//            String password = "GcaCWxn2prfNFW0eEGpNC9u8d+bkhGt4nyILovb5fn0=";
//            String salt = "@#$sbI&L";
//
//            // Encrypt the string
//            String encryptedString = encryptAndEncode(plainString, password, salt);
//            System.out.println("Encrypted: " + encryptedString);
//
//            // Decrypt the string
//            String decryptedString = decryptAndDecode(encryptedString, password, salt);
//            System.out.println("Decrypted: " + decryptedString);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
