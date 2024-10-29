package com.jodo.portal.security;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodo.portal.security.encryption.JWTEncryptionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPointJWT implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJWT.class);
	private JWTEncryptionUtility jwtUtility = new JWTEncryptionUtility(false);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		logger.error("Unauthorized error: {}", authException.getMessage());
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		String bearertoken = request.getHeader("access_token");
		String token = jwtUtility.getJWTfromHeader(bearertoken);
		logger.error("JWT Token: {}", token);

		Map<String, Object> body = createResponseBody(request, HttpStatus.UNAUTHORIZED.value());

		if (token == null) {
			setResponseMessage(body, response, "Access Token Not Found");
		} else {
			String validationMessage = jwtUtility.validateJwtToken(token);
			logger.error("JWT Token error message : {}", validationMessage);
			if (!"success".equals(validationMessage)) {
				setResponseMessage(body, response, validationMessage);
			} else {
				setResponseMessage(body, response, "You do not have sufficient access rights for this API.");
			}
		}
	}

	private Map<String, Object> createResponseBody(HttpServletRequest request, int status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", status);
		body.put("statusdescription", "UNAUTHORIZED");
		body.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		body.put("path", request.getServletPath());
		return body;
	}

	private void setResponseMessage(Map<String, Object> body, HttpServletResponse response, String message)
			throws IOException {
		body.put("message", message);
		logger.info("Response body: {}", body); 
		new ObjectMapper().writeValue(response.getOutputStream(), body);
		response.flushBuffer(); 
	}
}
