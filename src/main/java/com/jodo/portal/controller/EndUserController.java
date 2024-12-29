package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.jodo.portal.dto.EndUserDTO;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.model.AuthUserDetails;
import com.jodo.portal.model.ResponseApi;

import io.jsonwebtoken.Header;

import com.jodo.portal.implementation.EndUserServiceImplementation;
//import com.jodo.portal.security.util.JWTUtility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class EndUserController {

	private EndUserServiceImplementation userServiceImplementation;
	LoginController login = new LoginController();

	@Autowired
	public EndUserController(EndUserServiceImplementation userServiceImplementation, LoginController login) {
		this.userServiceImplementation = userServiceImplementation;
		this.login = login;
	}

	@PostMapping("/superadmin")
	public ResponseEntity<ResponseApi> addSuperAdminUser(@RequestBody EndUserDTO user) throws Exception {
		String roles = "SUPERADMIN,ADMIN,USER";
		List<EndUserDTO> savedUser = userServiceImplementation.addUser(user, roles);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "SuperAdmin registered successfully.", savedUser));
	}

	@PostMapping("/admin")
	public ResponseEntity<ResponseApi> addAdminUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
		String roles = "ADMIN,USER";
		HashMap<String, String> heeaderRequest = new HashMap<>();
		AuthUserDetails auth = login.getAuthDetails(request);
		List<EndUserDTO> savedUser = userServiceImplementation.addUser(user, roles);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.createResponse(1, "Admin registered successfully.", savedUser));
	}

	@PostMapping("/user")
	public ResponseEntity<ResponseApi> addEndUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
		String roles = "USER";
		List<EndUserDTO> savedUser = userServiceImplementation.addUser(user, roles);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "User registered successfully.", savedUser));
	}

	@PutMapping("/user")
	public ResponseEntity<ResponseApi> editEndUser(@RequestBody EndUserDTO user, HttpServletRequest request)
			throws Exception {
		List<EndUserDTO> savedUser = userServiceImplementation.editUser(user);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(ResponseApi.createResponse(1, "User details updated successfully.", savedUser));
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<ResponseApi> getEndUserById(@PathVariable("id") Long id, HttpServletRequest request)
			throws Exception {
		List<EndUserDTO> retrievedUser = userServiceImplementation.getUserById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "User details retrieved successfully", retrievedUser));
	}

	@GetMapping("/user/get_users")
	public ResponseEntity<ResponseApi> getAllEndUsers(HttpServletRequest request) throws Exception {
		List<EndUserDTO> endUserList = userServiceImplementation.getAllUsers();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All user details retrieved successfully.", endUserList));
	}

	@GetMapping("/user/roles/{userid}")
	public ResponseEntity<ResponseApi> getEndUserRoles(@PathVariable("userid") Long userid, HttpServletRequest request)
			throws Exception {
		ArrayList<Map<String, Object>> endUserList = userServiceImplementation.getAllUsersNative(userid);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "User roles retrieved successfully.", endUserList));
	}

	@DeleteMapping("/user/{id}")
	public ResponseEntity<ResponseApi> deleteEndUserById(@PathVariable Long id, HttpServletRequest request)
			throws Exception {
		List<String> endUserList = userServiceImplementation.disableUser(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "User with User ID " + id + " deleted successfully.", endUserList));
	}
}
