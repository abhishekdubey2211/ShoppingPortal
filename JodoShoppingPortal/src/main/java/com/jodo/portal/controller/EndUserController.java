package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.jodo.portal.dto.EndUserDTO;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.implementation.EndUserServiceImplementation;
import com.jodo.portal.security.EncryptionUtil;
import com.jodo.portal.security.util.JWTUtility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class EndUserController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JWTUtility jwtUtility;

	@Autowired
	private EndUserServiceImplementation userServiceImplementation;

	@Autowired
	private EncryptionUtil encryptionUtil;

	private Long validateSession(HttpServletRequest request) throws Exception {
		String sessionId = request.getHeader("ACS_SESSION_ID");

		// Check if sessionId is present in the request header
		if (sessionId == null || sessionId.isEmpty()) {
			throw new CustomException(403, "FORBIDDEN", "Session ID not found");
		}

		Map<String, String> sessionDetails = userServiceImplementation.checkSessionIsActive(sessionId);

		if (sessionDetails.containsKey("error")) {
			throw new CustomException(403, "FORBIDDEN", sessionDetails.get("error"));
		}

		try {
			return Long.parseLong(sessionDetails.get("userid"));
		} catch (NumberFormatException e) {
			throw new CustomException(400, "BAD_REQUEST", "Invalid user ID format");
		}
	}

	@PostMapping("/v1/superadmin")
	public ResponseEntity<ResponseApi> addSuperAdminUser(@RequestBody EndUserDTO user) throws Exception {
		String roles = "SUPERADMIN,ADMIN,USER";
		EndUserDTO savedUser = userServiceImplementation.addUser(user, roles);
		List<Object> endUserList = new ArrayList<>();
		endUserList.add(savedUser);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "SuperAdmin registered successfully.", endUserList));
	}

	@PostMapping("/v2/admin")
	public ResponseEntity<ResponseApi> addAdminUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
		Long sessionUserid = validateSession(request);
		if (sessionUserid < 0) {
			throw new CustomException(403, "Session ID not found", "FORBIDEN");
		}
		String roles = "ADMIN,USER";
		EndUserDTO savedUser = userServiceImplementation.addUser(user, roles);
		List<Object> endUserList = new ArrayList<>();
		endUserList.add(savedUser);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "Admin registered successfully.", endUserList));
	}

	@PostMapping("/v3/user")
	public ResponseEntity<ResponseApi> addEndUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		String roles = "USER";
		EndUserDTO savedUser = userServiceImplementation.addUser(user, roles);
		List<Object> endUserList = new ArrayList<>();
		endUserList.add(savedUser);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "User registered successfully.", endUserList));
	}

	@PutMapping("/v3/user")
	public ResponseEntity<ResponseApi> editEndUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
		// Validate session
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		EndUserDTO savedUser = userServiceImplementation.editUser(user);
		List<Object> endUserList = new ArrayList<>();
		endUserList.add(savedUser);

		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(ResponseApi.createResponse(1, "User details updated successfully.", endUserList));
	}

	@GetMapping("/v3/user/{id}")
	public ResponseEntity<ResponseApi> getEndUserById(@PathVariable("id") Long id, HttpServletRequest request)
			throws Exception {
		// Validate session
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		EndUserDTO retrievedUser = userServiceImplementation.getUserById(id);
		List<Object> endUserList = new ArrayList<>();
		endUserList.add(retrievedUser);

		return ResponseEntity.status(HttpStatus.OK).body(ResponseApi.createResponse(1,
				"User details retrieved successfully for User ID: " + retrievedUser.getUserid(), endUserList));
	}

	@GetMapping("/v2/user/get_users")
	public ResponseEntity<ResponseApi> getAllEndUsers(HttpServletRequest request) throws Exception {
		// Validate session
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		List<Object> endUserList = userServiceImplementation.getAllUsers();

		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All user details retrieved successfully.", endUserList));
	}

	@GetMapping("/v3/user/roles/{userid}")
	public ResponseEntity<ResponseApi> getEndUserRoles(@PathVariable("userid") Long userid, HttpServletRequest request)
			throws Exception {
		// Validate session
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		ArrayList<Map<String, Object>> endUserList = userServiceImplementation.getAllUsersNative(userid);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "User roles retrieved successfully.", endUserList));
	}

	@DeleteMapping("/v2/user/{id}")
	public ResponseEntity<ResponseApi> deleteEndUserById(@PathVariable Long id, HttpServletRequest request)
			throws Exception {
		// Validate session
//		Long sessionUserid = validateSession(request);
//		if (sessionUserid < 0) {
//			throw new CustomException(403, "Session ID not found", "FORBIDEN");
//		}
		List<Object> endUserList = userServiceImplementation.disableUser(id);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "User with User ID " + id + " deleted successfully.", endUserList));
	}

	@PostMapping("/authenticate/{username}/{password}")
	public ResponseEntity<ResponseApi> authenticate(@PathVariable("username") String username,
			@PathVariable("password") String password) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			if (authentication.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
				UserDetails user = userServiceImplementation.loadUserByUsername(username);
				String encSessionId = userServiceImplementation.generateSessionId(username);

				if (encSessionId == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(ResponseApi.createResponse(1, "Session Already Exists", "Previous Session Terminated"));
				}

				HashMap<String, Object> loginResponse = new HashMap<>();
				loginResponse.put("UserName", user.getUsername());
				loginResponse.put("access_token", jwtUtility.generateTokenfromUsername(user, encSessionId));
				loginResponse.put("roles",
						user.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList()));
				loginResponse.put("session_id", encSessionId);
				loginResponse.put("message", "Login Done Successfully");
				return ResponseEntity.status(HttpStatus.OK)
						.body(ResponseApi.createResponse(1, "Login Done Successfully", List.of(loginResponse)));
			} else {
				throw new CustomException(401, "Invalid User Credential", "Invalid Username or Password");
			}
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ResponseApi.createResponse(401, "Invalid User Credential", "Invalid Username or Password"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ResponseApi.createResponse(401, "Invalid User Credential", "Invalid Username or Password"));
		}
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) throws Exception {
		Long sessionUserid = validateSession(request);
		if (sessionUserid < 0) {
			throw new CustomException(403, "Session ID not found", "FORBIDEN");
		}
		String sessionId = request.getHeader("ACS_SESSION_ID");
		String islogout = userServiceImplementation.logout(sessionId);
		return islogout;
	}
}
