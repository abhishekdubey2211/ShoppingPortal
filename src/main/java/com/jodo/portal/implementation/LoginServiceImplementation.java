package com.jodo.portal.implementation;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.googlecode.gwt.crypto.bouncycastle.DataLengthException;
import com.googlecode.gwt.crypto.bouncycastle.InvalidCipherTextException;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.model.ActiveUserDetails;
import com.jodo.portal.model.AuthUserDetails;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.LoginRequest;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.ActiveUserRepository;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.security.encryption.JWTEncryptionUtility;
import com.jodo.portal.security.encryption.SecurityHandler;

@Service
public class LoginServiceImplementation {
	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImplementation.class);

	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private JWTEncryptionUtility jwtTokenUtility = new JWTEncryptionUtility(false);
	private JWTEncryptionUtility authorizationTokenUtility = new JWTEncryptionUtility(true);

	@Autowired
	EndUserServiceImplementation userServiceImplementation = new EndUserServiceImplementation();

	@Autowired
	private EnduserRepository enduserRepository;

	@Autowired
	private ActiveUserRepository activeUserRepository;

	@Autowired
	public RedisUtil redis;

	SecurityHandler securityHandler = new SecurityHandler();

	@Autowired
	private AuthenticationManager authenticationManager;

	public Map<String, Object> generateSessionId(String loginid, String sessionKey) throws Exception {
		try {
			Map<String, Object> mapResponse = new HashMap<>();
			EndUser user = enduserRepository.findByEmail(loginid).orElseThrow(() -> new Exception("User not found"));

			Optional<ActiveUserDetails> optionalSession = activeUserRepository.findByUseridAndActive(user.getId(), 1);
			if (optionalSession.isPresent()) {
				logout(optionalSession.get().getSessionid(), "FORCE_FULLY_LOGOUT");
				throw new CustomException(112, "Login Fail",
						"User " + optionalSession.get().getLoginid()
								+ " is already logged in. Forcefully logging out. Please re-login.",
						HttpStatus.BAD_REQUEST);
			}

			// Generating SessionId
			String sessionId = securityHandler.generateEncryptedSessionId(sessionKey);

			// Storing AuthUserDetails to redis
			AuthUserDetails authUserDetails = new AuthUserDetails();
			authUserDetails.setLoginId(user.getEmail());
			authUserDetails.setCartId(user.getCart().getId());
			authUserDetails.setUserId(user.getId());
			authUserDetails.setSessionId(sessionId);
			authUserDetails.setRoles(user.getRoles().toString());
			authUserDetails.setLogindatetime(sf.format(new Date()));

			System.out.println("Serialized AuthUserDetails: " + authUserDetails);
			logger.info("sessionKey  :: >>> " + sessionKey + "  || sessionId :: >>> " + sessionId);
			redis.set("ACTIVE_USER#" + sessionId, authUserDetails);

			// Storing ActiveUserDetails to db with sessionkey/RefreshToken
			ActiveUserDetails activeUser = ActiveUserDetails.builder().active(1).userid(user.getId())
					.loginid(user.getEmail()).logindatetime(sf.format(new Date())).logoutdatetime("")
					.sessionid(sessionKey).build();
			activeUserRepository.save(activeUser);

			Map<String, Object> mapAuthenticationTokes = generateSessionTokens(user, sessionId);

			return mapAuthenticationTokes;
		} catch (Exception e) {
			throw e;
		}
	}

	public Map<String, Object> generateSessionTokens(EndUser user, String strSessionid)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException,
			DataLengthException, IllegalStateException, InvalidCipherTextException {

		String strRefreshToken = securityHandler.generateDescryptedSessionId(strSessionid);
		logger.info("strSessionid :: >> " + strSessionid + " ||  strRefreshToken :: >> " + strRefreshToken);
		Optional<ActiveUserDetails> optionalRefresh = activeUserRepository.findBySessionidAndActive(strRefreshToken, 1);
		if (optionalRefresh.isPresent()) {
			Map<String, Object> accessTokenResponse = new HashMap<>();
			Map<String, Object> mapauthUser = new HashMap<>();
			mapauthUser.put("userid", user.getId());
			mapauthUser.put("cid", user.getCart().getId());
			mapauthUser.put("sessionid", strSessionid);
			mapauthUser.put("user_name", user.getUsername());
			mapauthUser.put("logindatetime", sf.format(new Date()));
			String JWTACCESS_TOKEN = jwtTokenUtility.generateTokenfromUsername(mapauthUser, user.getEmail());

			mapauthUser.remove("loginid");
			mapauthUser.remove("cid");
			mapauthUser.remove("userid");
			mapauthUser.remove("sessionid");
			mapauthUser.put("refresh_token", strRefreshToken);
			mapauthUser.put("password", user.getPassword());
			mapauthUser.put("authorities", user.getRoles());

			String AUTHORIZATION_TOKEN = authorizationTokenUtility.generateTokenfromUsername(mapauthUser,
					user.getEmail());
			accessTokenResponse.put("access_token", "Bearer " + JWTACCESS_TOKEN);
			accessTokenResponse.put("authorization_token", AUTHORIZATION_TOKEN);
			return accessTokenResponse;
		} else {
			throw new CustomException(401, "UNAUTHORIZED", "Invalid AccessToken or AuthorizationToken not found ",
					HttpStatus.UNAUTHORIZED);
		}
	}

	public String logout(String sessionId, String logoutReason) throws Exception {
		try {
			Optional<ActiveUserDetails> optionalSession = activeUserRepository.findBySessionidAndActive(sessionId, 1);
			if (optionalSession.isEmpty()) {
				throw new CustomException(113, "BAD_REQUEST", "Invalid Session Id", HttpStatus.BAD_REQUEST);
			}
			ActiveUserDetails activeUserDetails = optionalSession.get();
			if (activeUserDetails.getActive() == 0) {
				throw new CustomException(114, "BAD_REQUEST", "Expired Session Id", HttpStatus.BAD_REQUEST);
			}
			activeUserDetails.setActive(0);
			activeUserDetails.setLogoutreason(logoutReason);
			activeUserDetails.setLogoutdatetime(sf.format(new Date()));
			activeUserRepository.save(activeUserDetails);
			redis.delete("ACTIVE_USER#" + sessionId);
			logger.info("Session {} logged out successfully.", sessionId);
			return "Logout Done Successfully";
		} catch (Exception e) {
			throw e;
		}
	}

	public Map<String, Object> refreshSessionTokens(String strExpiredJwtToken) throws NumberFormatException, Exception {
		boolean isvalid = false;
		try {
			isvalid = jwtTokenUtility.isTokenExpired(strExpiredJwtToken);
		} catch (Exception e) {
			String errorResponse = jwtTokenUtility.validateJwtToken(strExpiredJwtToken);
			throw new CustomException(115, "BAD_REQUEST", errorResponse, HttpStatus.BAD_REQUEST);
		}
		if (isvalid) {
			String authSessionid = (String) jwtTokenUtility.getClaimFromToken(strExpiredJwtToken, "sessionid");
			int userid = (int) jwtTokenUtility.getClaimFromToken(strExpiredJwtToken, "userid");
			EndUser user = enduserRepository.findById(Long.valueOf(userid))
					.orElseThrow(() -> new RuntimeException("User not found"));
			Map<String, Object> mapAuthenticationTokes = generateSessionTokens(user, authSessionid);
			return mapAuthenticationTokes;
		} else {
			throw new CustomException(116, "BAD_REQUEST", "Token Not yet Expired", HttpStatus.BAD_REQUEST);
		}
	}

	public Map<String, Object> getSessionDetailsFromRedis(String jwtToken) throws Exception {
		Map<String, Object> sessionResponse = new HashMap<>();
		try {
			String validationResponse = jwtTokenUtility.validateJwtToken(jwtToken);
			if (!validationResponse.equals("success")) {
				throw new CustomException(417, "UNAUTHORIZED", validationResponse, HttpStatus.UNAUTHORIZED);
			}
			String sessionId = (String) jwtTokenUtility.getClaimFromToken(jwtToken, "sessionid");
			logger.info("sessionid  :: " + sessionId);

			String redisSessionDetails = redis.get("ACTIVE_USER#" + sessionId);
			System.out.println("Redis Session Details: " + redisSessionDetails);
			if (redisSessionDetails == null || redisSessionDetails.isEmpty()) {
				throw new CustomException(117, "BAD_REQUEST", "Invalid SessionId not Found in Redis ",
						HttpStatus.BAD_REQUEST);
			}

			try {
				AuthUserDetails activeUserDetails = new Gson().fromJson(redisSessionDetails, AuthUserDetails.class);
				sessionResponse.put("success", activeUserDetails);
			} catch (JsonSyntaxException e) {
				throw new CustomException(500, "INTERNAL_SERVER_ERROR",
						"Invalid session data in Redis: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return sessionResponse;
		} catch (Exception e) {
			throw e;
		}
	}

	public AuthUserDetails getAuthorizationDetails(Map<String, String> JsonWebToken) throws Exception {
		Map<String, Object> response = new HashMap<>();

		String strAccessToken = JsonWebToken.get("access_token");

		// Removing Bearer and get JWT
		String strAccessTokenHeader = jwtTokenUtility.getJWTfromHeader(strAccessToken);

		// Validate the provided AccessToken
		String accessTokenvalidationResponse = jwtTokenUtility.validateJwtToken(strAccessTokenHeader);
		if (!accessTokenvalidationResponse.equals("success")) {
			throw new CustomException(416, "UNAUTHORIZED", accessTokenvalidationResponse, HttpStatus.UNAUTHORIZED);
		}

		// If validated get sessionid from it
		String jwtSessionid = (String) jwtTokenUtility.getClaimFromToken(strAccessTokenHeader, "sessionid");

		if (jwtSessionid == null) {
			logger.info("strAccessTokenHeader >>  " + strAccessTokenHeader);
			throw new CustomException(415, "UNAUTHORIZED", "Invalid access_token", HttpStatus.UNAUTHORIZED);
		}

		String strAutherizationToken = JsonWebToken.get("authorization_token");

		// Validate the Auth Token
		String authTokenvalidationResponse = authorizationTokenUtility.validateJwtToken(strAutherizationToken);
		if (!authTokenvalidationResponse.equals("success")) {
			throw new CustomException(414, "UNAUTHORIZED", authTokenvalidationResponse, HttpStatus.UNAUTHORIZED);
		}

		// If validated get sessionid from it
		String authSessionid = (String) authorizationTokenUtility.getClaimFromToken(strAutherizationToken,
				"refresh_token");

		if (authSessionid == null) {
			logger.info("authSessionid >>  " + authSessionid);
			throw new CustomException(413, "UNAUTHORIZED", "Invalid authorization_token", HttpStatus.UNAUTHORIZED);
		}

		try {
			Map<String, Object> mapAuthdetails = getSessionDetailsFromRedis(strAccessTokenHeader);

			if (mapAuthdetails.containsKey("success")) {
				Object successData = mapAuthdetails.get("success");

				if (successData instanceof AuthUserDetails) {
					AuthUserDetails authUserDetails = (AuthUserDetails) successData;
					Boolean isvalidauth = false;
					try {
						isvalidauth = securityHandler.generateDescryptedSessionId(authUserDetails.getSessionId())
								.equals(authSessionid);
						if (isvalidauth == true) {
							return authUserDetails;
						} else {
							logger.error("Some Exception occured  isvalidauth :: " + isvalidauth);
							throw new CustomException(412, "UNAUTHORIZED", "invalid authorization_token",
									HttpStatus.UNAUTHORIZED);
						}
					} catch (Exception e) {
						logger.error("Some Exception occured  isvalidauth :: " + isvalidauth);
						throw new CustomException(411, "UNAUTHORIZED", "AccessToken or AuthorizationToken invalid",
								HttpStatus.UNAUTHORIZED);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public Map<String, Object> getSessionDetails(@RequestBody Map<String, String> JsonWebToken) throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		String strWebtoken = JsonWebToken.get("access_token");
		String webtoken = jwtTokenUtility.getJWTfromHeader(strWebtoken);
		Map<String, Object> accessTokenDetails = jwtTokenUtility.getClaims(webtoken);

		String autherizationToken = JsonWebToken.get("authorization_token");
		Map<String, Object> autherizationTokenDetails = authorizationTokenUtility.getClaims(autherizationToken);

		String jwtSessionid = (String) jwtTokenUtility.getClaimFromToken(webtoken, "sessionid");

		String accessTokenvalidationResponse = jwtTokenUtility.validateJwtToken(webtoken);
		if (!accessTokenvalidationResponse.equals("success")) {
			throw new CustomException(410, "UNAUTHORIZED", accessTokenvalidationResponse, HttpStatus.UNAUTHORIZED);
		}

		if (jwtSessionid == null) {
			logger.info("webtoken >>  " + webtoken);
			throw new CustomException(409, "UNAUTHORIZED", "Invalid access_token", HttpStatus.UNAUTHORIZED);
		}

//		String authSessionid = (String) authorizationTokenUtility.getClaimFromToken(autherizationToken, "sessionid");

		String authTokenvalidationResponse = authorizationTokenUtility.validateJwtToken(autherizationToken);
		if (!authTokenvalidationResponse.equals("success")) {
			throw new CustomException(408, "UNAUTHORIZED", authTokenvalidationResponse, HttpStatus.UNAUTHORIZED);
		}
		String strRefreshToken = (String) authorizationTokenUtility.getClaimFromToken(autherizationToken,
				"refresh_token");
		if (strRefreshToken == null) {
			logger.info("strRefreshToken >>  " + strRefreshToken);
			throw new CustomException(407, "UNAUTHORIZED", "Invalid authorization_token", HttpStatus.UNAUTHORIZED);
		}

		logger.info("JWT sessionid  :  " + jwtSessionid + " ||   Auth strRefreshToken : " + strRefreshToken);

		Map<String, Object> mapAuthdetails = getSessionDetailsFromRedis(webtoken);

		if (mapAuthdetails.containsKey("success")) {
			Object successData = mapAuthdetails.get("success");
			if (successData instanceof AuthUserDetails) {
				AuthUserDetails authUserDetails = (AuthUserDetails) successData;
				Boolean isvalidauth = false;
				try {
					isvalidauth = authUserDetails.getSessionId()
							.equals(securityHandler.generateEncryptedSessionId(strRefreshToken));
				} catch (Exception e) {
					logger.error("Some Exception occured  isvalidauth :: " + isvalidauth);
					throw new CustomException(406, "UNAUTHORIZED", "authorization_token invalid",
							HttpStatus.UNAUTHORIZED);
				}
				response.put("isvalid_authorization_token", isvalidauth);
				response.put("auth_userdetails_redis", authUserDetails);
				response.put("accesstoken_payload", accessTokenDetails);
				response.put("authorizationtoken_payload", autherizationTokenDetails);
				return response;
			}
		}
		return null;
	}

	public Map<String, Object> authenticate(LoginRequest pushLogin, Boolean isAuthenticationRequired) throws Exception {
		String loginSessionKey = pushLogin.getLoginsessionkey();
		String loginId = pushLogin.getLoginid();
		String authKey = pushLogin.getAuthenticationkey();
		String decryptedSessionId = null;
		try {
			decryptedSessionId = securityHandler.generateDescryptedSessionId(loginSessionKey);
		} catch (Exception e) {
			throw new CustomException(119, "BAD_REQUEST", "Invalid Loginsessionkey or Authenticationkey");
		}

		// Validate session key and authentication key
		String storedAuthKey = redis.getHashField("LOGINSESSIONKEY#" + loginSessionKey, decryptedSessionId);
		logger.info("StoredAuthKey: {} | ProvidedAuthKey: {} | SessionKey: {}", storedAuthKey, authKey,
				loginSessionKey);

		if (storedAuthKey == null || !authKey.trim().equals(storedAuthKey.replace("\"", "").trim())) {
			throw new CustomException(120, "BAD_REQUEST", "Invalid Loginsessionkey or Authenticationkey");
		}

		try {
			if (isAuthenticationRequired) {
				// Authenticate user credentials only if Authentication is with password
				Authentication authentication = authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(loginId, pushLogin.getPassword()));

				if (!authentication.isAuthenticated()) {
					throw new CustomException(408, "UNAUTHORIZED", "Invalid Username or Password",
							HttpStatus.UNAUTHORIZED);
				}
				// Clear session key after successful authentication and return token details
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (BadCredentialsException e) {
			throw new CustomException(418, "UNAUTHORIZED", "Invalid Username or Password", HttpStatus.UNAUTHORIZED);
		}
//			redis.delete("LOGINSESSIONKEY#" + loginSessionKey);
//			redis.delete( "USER_LOGINOTP#" + pushLogin.getLoginid());
		return generateSessionId(loginId, authKey);
	}

	public Map<String, Object> generateKey()
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		String strKey1 = securityHandler.generateRandomKey(30);
		String strKey2 = UUID.randomUUID().toString();
		String strEncKey1 = securityHandler.generateEncryptedSessionId(strKey1);
		redis.setHashFieldWithExpiration("LOGINSESSIONKEY#" + strEncKey1, strKey1, strKey2, 24, TimeUnit.HOURS);
		Map<String, Object> loginserviceResponse = new HashMap<>();
		loginserviceResponse.put("loginsessionkey", strEncKey1);
		loginserviceResponse.put("authenticationkey", strKey2);
		return loginserviceResponse;
	}

}
