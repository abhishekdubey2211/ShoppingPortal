package com.jodo.portal.security;

import static org.junit.Assert.assertThrows;

import java.awt.PageAttributes.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.security.util.JWTUtility;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPointJWT implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJWT.class);

	@Autowired
	JWTUtility jwtUtility;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		logger.error("Unauthorized error ", authException.getMessage());
		response.setContentType("application/json");

		String token = jwtUtility.getJWTfromHeader(request);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		final Map<String, Object> body = new HashMap<>();
		body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		body.put("error", "UNAUTHORIZED");
		body.put("path", request.getServletPath());

		if (token == null) {
			body.put("message", "Authorization Token Not Found");
			final ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(response.getOutputStream(), body);
		}

		if (!jwtUtility.validateJwtToken(token).equals("success")) {
			body.put("message", jwtUtility.validateJwtToken(token));
			final ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(response.getOutputStream(), body);
		}
	}

}
