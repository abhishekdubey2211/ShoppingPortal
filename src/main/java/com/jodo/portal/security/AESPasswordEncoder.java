package com.jodo.portal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jodo.portal.security.encryption.SecurityHandler;



public class AESPasswordEncoder implements PasswordEncoder {

    private  SecurityHandler aesUtil = new SecurityHandler();
	
	@Override
    public String encode(CharSequence rawPassword) {
        try {
            return aesUtil.generateEncryptedSessionId(rawPassword.toString());
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String decryptedPassword = aesUtil.generateDescryptedSessionId(encodedPassword);
            return rawPassword.toString().equals(decryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
