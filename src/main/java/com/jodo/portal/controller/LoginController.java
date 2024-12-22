package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.gwt.crypto.bouncycastle.DataLengthException;
import com.googlecode.gwt.crypto.bouncycastle.InvalidCipherTextException;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.implementation.EndUserServiceImplementation;
import com.jodo.portal.implementation.LoginServiceImplementation;
import com.jodo.portal.model.ActiveUserDetails;
import com.jodo.portal.model.AuthUserDetails;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.LoginRequest;
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.security.encryption.JWTEncryptionUtility;
import com.jodo.portal.security.encryption.SecurityHandler;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/login")
@Service
public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	private SecurityHandler security = new SecurityHandler();
	@Autowired
	private final EndUserServiceImplementation userEndUserServiceImplementation = new EndUserServiceImplementation();
	@Autowired
	private final RedisUtil redis;
	private final JWTEncryptionUtility jwtTokenUtility;
	private final JWTEncryptionUtility authorizationTokenUtility;

	@Autowired
	LoginServiceImplementation loginServiceImplementation = new LoginServiceImplementation();

	public LoginController() {
		this.redis = new RedisUtil();
		this.jwtTokenUtility = new JWTEncryptionUtility(false);
		this.authorizationTokenUtility = new JWTEncryptionUtility(true);
	}

	@PostMapping("/generatekey")
	public ResponseEntity<ResponseApi> generateKeyService(HttpServletRequest request) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		Map<String, Object> generatedKey = loginServiceImplementation.generateKey();
		headers.add("loginsessionkey", (String) generatedKey.get("loginsessionkey"));
		return ResponseEntity.status(HttpStatus.OK).headers(headers)
				.body(ResponseApi.createResponse(1, "Keys Generated Sucesssfully", List.of(generatedKey)));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<ResponseApi> loginService(@RequestBody LoginRequest pushLogin, HttpServletRequest request)
			throws Exception {
		HttpHeaders headers = new HttpHeaders();
		Map<String, Object> authToken = new HashMap<>();
		try {
			ErrorStatusDetails validationError = validateLoginRequest(pushLogin);
			if (validationError != null) {
				throw new CustomException(validationError.status(), "BAD_REQUEST", validationError.statusdescription());
			}

			if (pushLogin.getMethod().equalsIgnoreCase("OTP")) {
					if ((pushLogin.getOtp() != null || pushLogin.getOtp() != "")
							&& (pushLogin.getLoginid() != null || pushLogin.getLoginid() != "")) {
						String userOTP = pushLogin.getOtp();
						String retrivedUserOtp = redis.get("USER_LOGINOTP#" + pushLogin.getLoginid());
						logger.info(
								"redis retrived otp = " + retrivedUserOtp + " || userpassed otp " + pushLogin.getOtp());
						if (retrivedUserOtp ==null) {
							throw new CustomException(111, "BAD_REQUEST", "Invalid User OTP");
						}
						
						if (!userOTP.equals(retrivedUserOtp.replace("\"", "").trim())) {
							throw new CustomException(110, "BAD_REQUEST", "Invalid  User OTP");
						}
						authToken = loginServiceImplementation.authenticate(pushLogin, false);
					} else {
						throw new CustomException(109, "BAD_REQUEST", "OTP and Loginid is Mendatory", HttpStatus.BAD_REQUEST);
					}
			} else if (pushLogin.getMethod().equalsIgnoreCase("PASSWORD")) {
				authToken = loginServiceImplementation.authenticate(pushLogin, true);
			} else {
				throw new CustomException(108, "BAD_REQUEST", "Invalid Authentication Method Type",
						HttpStatus.BAD_REQUEST);
			}

			if (authToken != null) {
				headers.add("loginsessionkey", pushLogin.getLoginsessionkey());
				headers.add("access_token", (String) authToken.get("access_token"));
				headers.add("authorization_token", (String) authToken.get("authorization_token"));
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(
						ResponseApi.createResponse(1, "Login Authentication Done Successfully", List.of(authToken)));
			} else {
				throw new CustomException(401, "UNAUTHORIZED", "Login Authentication failed.",
						HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@PostMapping("/refresh")
	public Map<String, Object> refreshToken(@RequestBody Map<String, Object> request)
			throws NumberFormatException, Exception {
		String JWTtoken = (String) request.get("token");
		return loginServiceImplementation.refreshSessionTokens(JWTtoken);
	}

	@GetMapping("/getauthdetails")
	public Map<String, Object> getAuthorizationTokenDetails(@RequestBody HashMap<String, String> tokens)
			throws Exception {
		return loginServiceImplementation.getSessionDetails(tokens);
	}

	AuthUserDetails getAuthDetails(HttpServletRequest request) throws Exception {
		HashMap<String, String> heeaderRequest = new HashMap<>();
		heeaderRequest.put("access_token", request.getHeader("access_token"));
		heeaderRequest.put("authorization_token", request.getHeader("authorization_token"));
		AuthUserDetails auth = getAuthorizationDetails(heeaderRequest);
		return auth;
	}

	@GetMapping("/getauthdetails/auth")
	public AuthUserDetails getAuthorizationDetails(@RequestBody HashMap<String, String> tokens) throws Exception {
		return loginServiceImplementation.getAuthorizationDetails(tokens);
	}

	@PostMapping("/otp/{useremail}")
	public ResponseEntity<ResponseApi> generateOtp(@PathVariable("useremail") String userEmail)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		Map<String, Object> response = new LinkedHashMap<>();

		Boolean isMailSend = userEndUserServiceImplementation.requestOtp(userEmail);
		if (isMailSend) {
			Map<String, Object> generatedKey = loginServiceImplementation.generateKey();
			response.put("loginsessionkey", generatedKey.get("loginsessionkey"));
			response.put("authenticationkey", generatedKey.get("authenticationkey"));
			response.put("status", "Login OTP Send Successfully on " + userEmail);
		} else {
			response.put("status", "Fail to Send Login OTP to " + userEmail);
		}

		if (isMailSend) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(ResponseApi.createResponse(1, "Login OTP Send Successfully", List.of(response)));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(ResponseApi.createResponse(0, "Fail to Send Login OTP", List.of(response)));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseApi> logoutActiveUser(@RequestBody HashMap<String, String> tokens) throws Exception {
		try {
			String jwtToken = tokens.get("token");
			Map<String, Object> mapAuthdetails = loginServiceImplementation.getSessionDetailsFromRedis(jwtToken);
			if (mapAuthdetails.containsKey("success")) {
				Object successData = mapAuthdetails.get("success");
				if (successData instanceof AuthUserDetails authUserDetails) {
					Boolean isvalidauth = false;
					try {
						String strSessionId = security.generateDescryptedSessionId(authUserDetails.getSessionId());
						String response = loginServiceImplementation.logout(strSessionId, "USER_LOGOUT");
						return ResponseEntity.status(HttpStatus.OK)
								.body(ResponseApi.createResponse(1, "Login OTP Send Successfully", List.of(response)));
					} catch (Exception e) {
						logger.error("Some Exception occured  isvalidauth :: " + isvalidauth);
						throw new CustomException(107, "UNAUTHORIZED", "AccessToken  invalid", HttpStatus.UNAUTHORIZED);
					}
				}
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(ResponseApi.createResponse(0, "Fail to Send OTP", List.of("Fail to Send OTP")));
		} catch (Exception e) {
			throw e;
		}
	}

	private ErrorStatusDetails validateLoginRequest(LoginRequest loginRequest) {
		if (loginRequest.getLoginid() == null || loginRequest.getLoginid().isEmpty()) {
			return new ErrorStatusDetails(101, "LoginId is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (loginRequest.getPassword() == null) {
			return new ErrorStatusDetails(102, "Password is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (loginRequest.getLoginsessionkey() == null || loginRequest.getLoginsessionkey().isEmpty()) {
			return new ErrorStatusDetails(103, "Login Session key is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (loginRequest.getAuthenticationkey() == null || loginRequest.getAuthenticationkey().isEmpty()) {
			return new ErrorStatusDetails(104, "Authentication key is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (loginRequest.getMethod() == null) {
			return new ErrorStatusDetails(105, "Authentication Method is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (!loginRequest.getMethod().equals("OTP") && !loginRequest.getMethod().equals("PASSWORD")) {
			return new ErrorStatusDetails(106, "Invalid Authentication Method. It should be OTP or PASSWORD",
					HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	public record ErrorStatusDetails(int status, String statusdescription, HttpStatus errorStatus) {
	}

}
