package com.jodo.portal.security.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESDerivedKeyEncryption {

	private static final String AES = "AES";
	private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
	private static final String PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
	private static final String AES_KEY_WRAP = "AESWrap";
	private static final int GCM_TAG_LENGTH = 128;
	private static final int AES_KEY_SIZE = 256;
	private static final int PBKDF2_ITERATIONS = 100000; // Increased iterations
	private static final int PBKDF2_KEY_LENGTH = 256;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int GCM_IV_LENGTH = 16;

	// Derive a key from a password
	public static String deriveKey(String password, String saltBase64) throws Exception {
		byte[] salt = Base64.getDecoder().decode(saltBase64);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_HMAC_SHA256);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), AES);
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}

	// Generate a random salt
	public static String generateSalt() {
		byte[] salt = new byte[16]; // 128-bit salt
		SECURE_RANDOM.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	// Generate a random AES key
	public static String generateAESKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance(AES);
		keyGen.init(AES_KEY_SIZE, SECURE_RANDOM);
		SecretKey secretKey = keyGen.generateKey();
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}

	// Encrypt the AES key with a password-derived key
	public static String encryptAESKey(String aesKeyBase64, String wrappingKeyBase64) throws Exception {
		SecretKey aesKey = new SecretKeySpec(Base64.getDecoder().decode(aesKeyBase64), AES);
		SecretKey wrappingKey = new SecretKeySpec(Base64.getDecoder().decode(wrappingKeyBase64), AES);
		Cipher cipher = Cipher.getInstance(AES_KEY_WRAP);
		cipher.init(Cipher.WRAP_MODE, wrappingKey);
		byte[] wrappedKey = cipher.wrap(aesKey);
		return Base64.getEncoder().encodeToString(wrappedKey);
	}

	// Encrypt a plaintext string
	public static String encrypt(String plaintext, String keyBase64) throws Exception {
		SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyBase64), AES);
		Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
		byte[] iv = new byte[GCM_IV_LENGTH];
		SECURE_RANDOM.nextBytes(iv); // Securely generate the IV
		GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
		byte[] encryptedText = cipher.doFinal(plaintext.getBytes());
		byte[] encryptedTextWithIv = new byte[GCM_IV_LENGTH + encryptedText.length];
		System.arraycopy(iv, 0, encryptedTextWithIv, 0, GCM_IV_LENGTH);
		System.arraycopy(encryptedText, 0, encryptedTextWithIv, GCM_IV_LENGTH, encryptedText.length);
		return Base64.getEncoder().encodeToString(encryptedTextWithIv);
	}

	public static String decrypt(String encryptedTextBase64, String keyBase64) throws Exception {
		byte[] encryptedTextWithIv = Base64.getDecoder().decode(encryptedTextBase64);

		// Extract the IV
		byte[] iv = new byte[GCM_IV_LENGTH];
		System.arraycopy(encryptedTextWithIv, 0, iv, 0, GCM_IV_LENGTH);

		// Extract the encrypted text (excluding the IV part)
		byte[] encryptedText = new byte[encryptedTextWithIv.length - GCM_IV_LENGTH];
		System.arraycopy(encryptedTextWithIv, GCM_IV_LENGTH, encryptedText, 0, encryptedText.length);

		// Decrypt
		SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyBase64), AES);
		Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
		GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

		byte[] decryptedText = cipher.doFinal(encryptedText);
		return new String(decryptedText);
	}

	public static void main(String[] args) {
		try {
			// Example plaintext and password
			String plaintext = "Hello, World!";
			String password = "strongpassword";

			// Generate a salt
			String salt = AESDerivedKeyEncryption.generateSalt();
			System.out.println("Salt: " + salt);

			// Derive a key from the password and salt
			String derivedKey = AESDerivedKeyEncryption.deriveKey(password, salt);
			System.out.println("Derived Key: " + derivedKey);

			// Encrypt the plaintext using the derived key
			String encryptedText = AESDerivedKeyEncryption.encrypt(plaintext, derivedKey);
			System.out.println("Encrypted Text: " + encryptedText);

			// Decrypt the encrypted text using the same derived key
			String decryptedText = AESDerivedKeyEncryption.decrypt(encryptedText, derivedKey);
			System.out.println("Decrypted Text: " + decryptedText);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}