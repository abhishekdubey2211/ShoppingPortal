package com.jodo.portal.security.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.jodo.portal.security.EncryptionUtil;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AESEncryptionUtility {

	private String encryptString(String text, String password, Boolean isCompress) throws Exception {
		// Convert password to UTF-8 bytes
		byte[] baPwd = password.getBytes(StandardCharsets.UTF_8);

		// Hash the password with SHA-256
		byte[] baPwdHash = MessageDigest.getInstance("SHA-256").digest(baPwd);

		// Convert text to UTF-8 bytes
		byte[] baText = text.getBytes(StandardCharsets.UTF_8);

		if (isCompress) {
			baText = compress(baText);
		}
		String saltStr = "@#$sbI&L";
		byte[] baSalt = saltStr.getBytes(StandardCharsets.US_ASCII);

		// Combine Salt + Text
		byte[] baEncrypted = new byte[baSalt.length + baText.length];
		System.arraycopy(baSalt, 0, baEncrypted, 0, baSalt.length);
		System.arraycopy(baText, 0, baEncrypted, baSalt.length, baText.length);

		// Perform AES Encryption
		baEncrypted = AES_Encrypt(baEncrypted, baPwdHash);
		return Base64.getEncoder().encodeToString(baEncrypted);
	}

	// Method to compress data securely
	private byte[] compress(byte[] data) throws IOException, NoSuchAlgorithmException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		// Use try-with-resources to ensure that the GZIPOutputStream is closed properly
		try (GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream)) {
			gzip.write(data);
		}

		// Get the compressed data
		byte[] compressedData = byteArrayOutputStream.toByteArray();

		// Verify data integrity by computing a hash
		byte[] hash = computeHash(data);
		return compressedData;
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private byte[] decompress(byte[] compressedData) throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
		try (GZIPInputStream gzip = new GZIPInputStream(byteArrayInputStream);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gzip.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, len);
			}
			return byteArrayOutputStream.toByteArray();
		}
	}

	// Decryption method
	private String decryptString(String encryptedText, String password, Boolean isCompressed) throws Exception {

		// Convert password to UTF-8 bytes
		byte[] baPwd = password.getBytes(StandardCharsets.UTF_8);

		// Hash the password with SHA-256
		byte[] baPwdHash = MessageDigest.getInstance("SHA-256").digest(baPwd);

		// Decode the encrypted text from Base64
		byte[] baEncrypted = Base64.getDecoder().decode(encryptedText);

		// Perform AES Decryption
		byte[] decryptedBytes = AES_Decrypt(baEncrypted, baPwdHash);

		// Extract salt and text from the decrypted data
		byte[] saltStr = "@#$sbI&L".getBytes(StandardCharsets.US_ASCII);
		byte[] baDecryptedText = new byte[decryptedBytes.length - saltStr.length];
		System.arraycopy(decryptedBytes, saltStr.length, baDecryptedText, 0, baDecryptedText.length);

		// Decompress if needed
		if (isCompressed) {
			baDecryptedText = decompress(baDecryptedText);
		}

		return new String(baDecryptedText, StandardCharsets.UTF_8);
	}

	// AES Decryption method
	public byte[] AES_Decrypt(byte[] bytesToBeDecrypted, byte[] passwordBytes) throws Exception {

		byte[] saltBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		// Derive the key and IV using PBKDF2
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		PBEKeySpec spec = new PBEKeySpec(new String(passwordBytes).toCharArray(), saltBytes, 1000, 256 + 128);
		SecretKey tmp = factory.generateSecret(spec);

		byte[] keyBytes = Arrays.copyOfRange(tmp.getEncoded(), 0, 32); // AES key
		byte[] ivBytes = Arrays.copyOfRange(tmp.getEncoded(), 32, 48); // IV

		// Initialize AES cipher for decryption
		SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

		// Decrypt the data
		byte[] decryptedBytes = cipher.doFinal(bytesToBeDecrypted);
		return decryptedBytes;
	}

	// Method to verify the integrity of decompressed data
	private boolean verifyDataIntegrity(byte[] originalData, byte[] decompressedData) throws NoSuchAlgorithmException {
		byte[] originalHash = computeHash(originalData);
		byte[] decompressedHash = computeHash(decompressedData);
		return MessageDigest.isEqual(originalHash, decompressedHash);
	}

	private byte[] computeHash(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(data);
	}

	private byte[] AES_Encrypt(byte[] bytesToBeEncrypted, byte[] passwordBytes) throws Exception {

		byte[] saltBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

		// Derive the key and IV using PBKDF2
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		PBEKeySpec spec = new PBEKeySpec(new String(passwordBytes).toCharArray(), saltBytes, 1000, 256 + 128);
		SecretKey tmp = factory.generateSecret(spec);

		byte[] keyBytes = Arrays.copyOfRange(tmp.getEncoded(), 0, 32); // AES key
		byte[] ivBytes = Arrays.copyOfRange(tmp.getEncoded(), 32, 48); // IV

		// Initialize AES cipher
		SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

		// Encrypt the data
		byte[] encryptedBytes = cipher.doFinal(bytesToBeEncrypted);
		return encryptedBytes;
	}

	public String encrypt(String originalData) throws Exception {
		return encryptString(originalData,EncryptionUtil.CRM_AESKEY,true);
	}
	
	public String decrypt(String encryptedData) throws Exception {
		return decryptString(encryptedData,EncryptionUtil.CRM_AESKEY,true);
	}
}
