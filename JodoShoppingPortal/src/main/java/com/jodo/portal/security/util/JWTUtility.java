package com.jodo.portal.security.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.exceptions.JwtValidationException;
import com.jodo.portal.security.EncryptionUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JWTUtility {

	private static final Logger logger = LoggerFactory.getLogger(JWTUtility.class);
	private String jwtSecrete = "D75451ABAEF6820A42AF5D6C33722A9DB9288B61863CF22B2B4F5BBD3677DD847B97F3AEBFE2BAAC111ECBF1033256"
			+ "39EE21E48025DD640109118257EAAD78B5";

	public String getJWTfromHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		logger.debug("Authorization Header :: {} ", bearerToken);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private Key key() {
		try {
			return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecrete));
		} catch (IllegalArgumentException e) {
			logger.error("Error generating key: {}", e.getMessage());
			throw e;
		}
	}

	public String generateTokenfromUsername(UserDetails userDetails, String sessionid) {
		try {
			Map<String, Object> claims = new HashMap<>();
			claims.put("role", userDetails.getAuthorities());
			claims.put("name", userDetails.getUsername());
			claims.put("password", userDetails.getPassword());
			claims.put("sessionid", sessionid);
			String jwtToken = Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date())
					.expiration(Date.from(Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(5)))).signWith(key())
					.compact();
			return EncryptionUtil.encryptString(jwtToken, true);
		} catch (Exception e) {
			logger.error("Error generating token from username: {}", e.getMessage());
			return null;
		}
	}

	public String getUsernameFromToken(String jwtToken) {
		try {
			return Jwts.parser().verifyWith((SecretKey) key()).build()
					.parseSignedClaims(EncryptionUtil.decryptString(jwtToken, true)).getPayload().getSubject();
		} catch (Exception e) {
			logger.error("Error getting username from token: {}", e.getMessage());
			return null;
		}
	}

	public Claims getClaims(String jwtToken) {
		try {
			return Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(EncryptionUtil.decryptString(jwtToken, true)).getPayload();
		} catch (Exception e) {
			logger.error("Error getting claims from token: {}", e.getMessage());
			return null;
		}
	}

	public boolean isTokenvalid(String jwtToken) throws Exception {
		Claims claims = getClaims(EncryptionUtil.decryptString(jwtToken, true));
		return claims.getExpiration().after(Date.from(Instant.now()));
	}

	public String validateJwtToken(String jwtToken) {
		try {
			Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims( EncryptionUtil.decryptString(jwtToken, true));
			return "success"; 
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT Token: {}", e.getMessage());
			return "Authorization token invalid";
		} catch (ExpiredJwtException e) {
			logger.error("JWT token expired: {}", e.getMessage());
			return "Authorization token has expired.";
		} catch (UnsupportedJwtException e) {
			logger.error("Unsupported JWT Token: {}", e.getMessage());
			return "Unsupported Authorization token invalid token";
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
			return "Authorization token invalid";
		} catch (Exception e) {
			logger.error("An unexpected error occurred during JWT validation: {}", e.getMessage());
			return "Authorization token not found.";
		}
	}
}
