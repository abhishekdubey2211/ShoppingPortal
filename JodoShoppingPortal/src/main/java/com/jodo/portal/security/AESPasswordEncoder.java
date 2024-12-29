package com.jodo.portal.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class AESPasswordEncoder implements PasswordEncoder {

    private final EncryptionUtil aesUtil;

    public AESPasswordEncoder() {
		this.aesUtil = new EncryptionUtil();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            return aesUtil.encryptString(rawPassword.toString(),  true);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String decryptedPassword = aesUtil.decryptString(encodedPassword, true);
            return rawPassword.toString().equals(decryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
