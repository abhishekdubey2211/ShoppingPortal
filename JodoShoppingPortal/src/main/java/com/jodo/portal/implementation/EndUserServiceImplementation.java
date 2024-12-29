package com.jodo.portal.implementation;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import com.jodo.portal.service.EndUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.jodo.portal.dto.EndUserDTO;
import com.jodo.portal.dto.EndUserMapper;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.ActiveUserDetails;
import com.jodo.portal.model.Cart;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Role;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.ActiveUserRepository;
import com.jodo.portal.repository.AddressRepository;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.repository.UserParameterdetailsRepository;
import com.jodo.portal.security.AdvanceEncryptionStandard;
import com.jodo.portal.security.EncryptionUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.*;

@Service
public class EndUserServiceImplementation implements UserDetailsService, EndUserService {
	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(EndUserServiceImplementation.class);
	private final EndUserMapper userMapper = new EndUserMapper();

	@Autowired
	ActiveUserRepository activeUserRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	public AdvanceEncryptionStandard aes;

	@Autowired
	private EnduserRepository enduserRepository;

	@Autowired
	public RedisUtil redis;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private UserParameterdetailsRepository parameterdetailsRepository;

	@Autowired
	EncryptionUtil encryptionUtil;

	@Override
	public EndUserDTO addUser(EndUserDTO pushUserDTO, String strRoles) {
		EndUser pushUser = userMapper.convertToUserEntity(pushUserDTO);
		logger.info(pushUser.toString());
		checkUserEmailExists(pushUser);
		pushUser.setRole(strRoles);
		Set<Role> roles = assignRoles(pushUser.getRole());
		if (pushUser.getPassword() == null || pushUser.getPassword().isEmpty()) {
			// IF LOGIN USING GOOGLE SET DEFALUT OR GENERATE RANDOM MAIL ON PROVIDED MAILID
			pushUser.setPassword(passwordEncoder.encode("Test1234!"));
			// GENERATE MAIL USING EVENT TO KAFKA
		} else {
			pushUser.setPassword(passwordEncoder.encode(pushUser.getPassword()));
		}
		pushUser.setIsactive(1);
		pushUser.setIsdelete(0);
		pushUser.setUsercreationdate(sf.format(new Date()));
		pushUser.setUserlastupdatedate("");
		pushUser.setRoles(roles);
		pushUser.getAddress().forEach(address -> address.setUser(pushUser));
		pushUser.getUserParameterDetails().forEach(parameter -> parameter.setUser(pushUser));
		pushUser.setCart(Cart.builder().totalamount(0.0).user(pushUser).build());
		EndUser saveduser = enduserRepository.save(pushUser);
		EndUserDTO savedDto = userMapper.convertToUserDTO(saveduser);
		updateCachedData(pushUserDTO);
		return savedDto;
	}

	@Override
	public EndUserDTO editUser(EndUserDTO putUserDTO) {
		EndUser putUser = userMapper.convertToUserEntity(putUserDTO);
		logger.info(putUser.toString());
		EndUser retrievedUser = enduserRepository.findById(putUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + putUser.getId()));
		if (retrievedUser.getIsactive() == 0 && retrievedUser.getIsdelete() == 1) {
			throw new ResourceNotFoundException("No user found with UserId " + putUser.getId());
		}

		if (!putUser.getEmail().equals(retrievedUser.getEmail())) {
			checkUserEmailExists(putUser);
		}

		retrievedUser.setContact(putUser.getContact());
		retrievedUser.setDesignation(putUser.getDesignation());
		retrievedUser.setEmail(putUser.getEmail());
		retrievedUser.setUsername(putUser.getUsername());
		retrievedUser.setIsactive(1);
		retrievedUser.setIsdelete(0);
		if (!putUser.getPassword().equals(retrievedUser.getPassword())) {
			String encryptedPassword = passwordEncoder.encode(putUser.getPassword());
			retrievedUser.setPassword(encryptedPassword);
		}
		retrievedUser.setProfileimage(putUser.getProfileimage());
		retrievedUser.setUserlastupdatedate(sf.format(new Date()));
		retrievedUser.getAddress().clear();
		putUser.getAddress().forEach(address -> {
			address.setUser(retrievedUser); // Set the correct relationship
			retrievedUser.getAddress().add(address);
		});
		retrievedUser.getUserParameterDetails().clear();
		putUser.getUserParameterDetails().forEach(parameter -> {
			parameter.setUser(retrievedUser); // Set the correct relationship
			retrievedUser.getUserParameterDetails().add(parameter);
		});
		EndUser updatedUser = enduserRepository.save(retrievedUser);
		EndUserDTO updatedDto = userMapper.convertToUserDTO(updatedUser);
		redis.delete("USER#" + updatedUser.getId());
		redis.setWithDefaultExpiration("USER#" + updatedUser.getId(), updatedDto);
		updateCachedData(putUserDTO);
		return updatedDto;
	}

	@Override
	public ArrayList<Map<String, Object>> getAllUsersNative(Long nUserid) {
		String sql = "SELECT user_id, role FROM user_roles WHERE user_id = :userId";
		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("userId", nUserid);
		ArrayList<Map<String, Object>> roles = new ArrayList<>();
		List<Object[]> results = query.getResultList();
		int i = 0;
		for (Object[] row : results) {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("user_id", row[0]);
			resultMap.put("role", row[1]);
			roles.add(resultMap);
			i++;
		}
		return roles;
	}

	@Override
	public List<Object> getAllUsers() {
		String cacheKey = "ALL_USERS";
		String cachedUsers = redis.get(cacheKey);
		List<Object> usersList;
		if (cachedUsers != null && !cachedUsers.isEmpty()) {
			logger.info("Fetching users from cache");
			usersList = redis.convertStringToObjType(cachedUsers, new TypeToken<List<EndUserDTO>>() {
			});
		} else {
			logger.info("Fetching users from database");
			List<EndUser> users = enduserRepository.findAll().stream()
					.filter((user) -> user.getIsdelete() == 0 && user.getIsactive() == 1).collect(Collectors.toList());
			usersList = users.stream().map(userMapper::convertToUserDTO).collect(Collectors.toList());
			redis.setWithDefaultExpiration(cacheKey, usersList);
		}
		return usersList;
	}

	@Override
	public List<Object> disableUser(Long id) {
		try {
			EndUser retrivedUser = getSingleUserByUserId(id);
			retrivedUser.setIsactive(0);
			retrivedUser.setIsdelete(1);
			retrivedUser.setUserlastupdatedate(sf.format(new Date()));
			EndUser savedUser = enduserRepository.save(retrivedUser);
//			redis.delete("USER#" + savedUser.getId());
//			redis.delete("ALL_USERS");
			return List.of("User with UserId " + id + " is deleted");
		} catch (Exception e) {
			logger.error("Unexpected error during disableUser", e);
			throw e;
		}
	}

	@Override
	public EndUserDTO getUserById(Long id) {
		try {
			return userMapper.convertToUserDTO(getSingleUserByUserId(id));
		} catch (Exception e) {
			logger.error("Unexpected error during user update", e);
			throw e;
		}
	}

	public boolean checkUserEmailExists(EndUser pushUser) {
		try {
			Optional<EndUser> existingUser = enduserRepository.findByEmail(pushUser.getEmail());
			if (existingUser.isPresent()) {
				if (existingUser.get().getIsdelete() == 0) {
					logger.warn("User with Email {} already exists", pushUser.getEmail());
					throw new CustomException(301, "User with Email " + pushUser.getEmail() + " already exists",
							"Email already Exists");
				}
			}
		} catch (Exception e) {
			logger.error("Unexpected error occured while checkUserEmailExists ", e);
			throw e;
		}
		return true;
	}

	private Set<Role> assignRoles(String roleString) {
		Set<Role> roles = new HashSet<>();
		if (roleString != null && !roleString.isEmpty()) {
			String[] roleArray = roleString.split(",");
			for (String roleName : roleArray) {
				if (isValidRole(roleName)) {
					roles.add(Role.valueOf(roleName.trim().toUpperCase()));
				} else {
					logger.warn("Invalid role name: {}", roleName);
				}
			}
		}
		if (roles.isEmpty()) {
			roles.add(Role.USER);
		}
		return roles;
	}

	public boolean isValidRole(String role) {
		try {
			Role.valueOf(role.toUpperCase());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public EndUser getSingleUserByUserId(long id) {
		String cachedUser = redis.get("USER#" + id);
		logger.info("cachedUser :: {}", cachedUser);
		EndUser user = new EndUser();
		if (cachedUser != null && !cachedUser.isEmpty()) {
			try {
				EndUserDTO userdto = redis.convertStringToObj(cachedUser, EndUserDTO.class);
				user = userMapper.convertToUserEntity(userdto);
				logger.info(" user cachedUser :: {}", user);
			} catch (Exception e) {
				logger.info(" user cachedUser :: {}", e);
			}
		} else {
			user = enduserRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + id));
			if (user.getIsactive() == 0 && user.getIsdelete() == 1) {
				throw new ResourceNotFoundException("No user found with UserId " + id);
			}
			redis.set("USER#" + user.getId(), userMapper.convertToUserDTO(user));
		}
		return user;
	}

	public void updateCachedData(EndUserDTO user) {
		redis.delete("USER#" + user.getUserid());
		redis.delete("ALL_USERS");
		redis.set("USER#" + user.getUserid(), user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<EndUser> userByName = enduserRepository.findByUsername(username);
		EndUser user = userByName.orElseGet(() -> enduserRepository.findByEmail(username).orElseThrow(
				() -> new UsernameNotFoundException("User not found with username or email: " + username)));
		ArrayList<Map<String, Object>> roles = getAllUsersNative(user.getId());
		List<String> rolesList = roles.stream().map(roleMap -> (String) roleMap.get("role")).toList();
		String[] arrRoles = rolesList.toArray(new String[0]);

		return User.builder().username(user.getUsername()).password(user.getPassword()).roles(arrRoles).build();
	}
	@Override
	public Map<String, String> checkSessionIsActive(String sessionId) throws Exception {
	    Optional<ActiveUserDetails> optionalSession = activeUserRepository.findBySessionid(sessionId);
	    Map<String, String> sessionDetails = new HashMap<>();
	    if (optionalSession.isEmpty()) {
	        sessionDetails.put("error", "Invalid Session ID");
	        return sessionDetails;
	    }
	    ActiveUserDetails userDetails = optionalSession.get();
	    if (userDetails.getActive() == 0) {
	        sessionDetails.put("error", "Expired Session ID");
	        return sessionDetails;
	    }
	    return getSessionDetails(sessionId);
	}

	public Map<String, String> getSessionDetails(String sessionId) throws Exception {
		String key = aes.decodeEncryptedKey(sessionId);
		String[] authDetails = key.split("##");
		if (authDetails.length != 3) {
			throw new Exception("Invalid session details");
		}
		Map<String, String> sessionDetails = new HashMap<>();
		sessionDetails.put("username", authDetails[0]);
		sessionDetails.put("userid", authDetails[2]);
		logger.info("Session details" + sessionDetails);
		return sessionDetails;
	}

	@Override
	public String generateSessionId(String username) throws Exception {
		EndUser user = enduserRepository.findByUsername(username).or(() -> enduserRepository.findByEmail(username))
				.orElseThrow(() -> new Exception("User not found"));
		String randomKey = EncryptionUtil.generateRandomKey(5);
		String encryptionKey = username + "##" + randomKey + "##" + user.getId();
		String sessionid = aes.encrypt(encryptionKey);
		Optional<ActiveUserDetails> optionalSesssion = activeUserRepository.findByUseridAndActive(user.getId(), 1);
		if (optionalSesssion.isPresent()) {
			logout(optionalSesssion.get().getSessionid());
			return null;
		}
		ActiveUserDetails activeuser = ActiveUserDetails.builder().active(1).userid(user.getId())
				.username(user.getUsername()).logindatetime(sf.format(new Date())).logoutdatetime("")
				.sessionid(sessionid).build();
		activeUserRepository.save(activeuser);
		return sessionid;
	}

	@Override
	public String logout(String sessionId) throws Exception {
		Optional<ActiveUserDetails> optionalSesssion = activeUserRepository.findBySessionid(sessionId);
		if (optionalSesssion.isEmpty()) {
			return "Invalid Session Id";
		}
		if (optionalSesssion.get().getActive() == 0) {
			return "Expired Session Id";
		}
		ActiveUserDetails details = optionalSesssion.get();
		details.setActive(0);
		details.setLogoutdatetime(sf.format(new Date()));
		activeUserRepository.save(details);

	    logger.info("Session {} logged out successfully.", sessionId);
	    return "Logged out successfully";
	}
}
