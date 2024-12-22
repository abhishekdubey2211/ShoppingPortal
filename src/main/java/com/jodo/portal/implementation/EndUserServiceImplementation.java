package com.jodo.portal.implementation;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jodo.portal.constants.MessageConstants;
import com.jodo.portal.controller.LoginController.ErrorStatusDetails;
import com.jodo.portal.dto.EndUserDTO;
import com.jodo.portal.dto.EndUserMapper;
import com.jodo.portal.exceptions.CustomException;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.ActiveUserDetails;
import com.jodo.portal.model.AuthUserDetails;
import com.jodo.portal.model.Cart;
import com.jodo.portal.model.Email;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Role;
import com.jodo.portal.model.UserParameterDetails;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.ActiveUserRepository;
import com.jodo.portal.repository.AddressRepository;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.repository.UserParameterdetailsRepository;
import com.jodo.portal.security.AdvanceEncryptionStandard;
import com.jodo.portal.security.encryption.EmailService;
import com.jodo.portal.security.encryption.SecurityHandler;
import com.jodo.portal.service.EndUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EndUserServiceImplementation implements EndUserService, MessageConstants, UserDetailsService {
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

	Gson gson = new Gson();

	@Override
	public List<EndUserDTO> addUser(EndUserDTO pushUserDTO, String strRoles) {
		ErrorStatusDetails error = pushUserDTO.validateEndUserRequest(pushUserDTO, "add");
		if (error != null) {
			throw new CustomException(error.status(), "BAD_REQUEST", error.statusdescription());
		}

		EndUser pushUser = userMapper.convertToUserEntity(pushUserDTO);
		logger.info(pushUser.toString());
		checkUserEmailExists(pushUser);
		pushUser.setRole(strRoles);
		Set<Role> roles = assignRoles(pushUser.getRole());
		if (pushUser.getPassword() == null || pushUser.getPassword().isEmpty()) {
			// IF LOGIN USING GOOGLE SET DEFALUT OR GENERATE RANDOM MAIL ON PROVIDED MAILID
			pushUser.setPassword(passwordEncoder.encode("Test1234!"));
//			pushUser.setPassword("Test1234!");

			// GENERATE MAIL USING EVENT TO KAFKA
		} else {
			pushUser.setPassword(passwordEncoder.encode(pushUser.getPassword()));
//			pushUser.setPassword(pushUser.getPassword());
		}
		pushUser.setIsactive(1);
		pushUser.setIsdelete(0);
		pushUser.setUsercreationdate(sf.format(new Date()));
		pushUser.setUserlastupdatedate("");
		pushUser.setRoles(roles);
		pushUser.getAddress().forEach(address -> address.setUser(pushUser));
		pushUser.setCart(Cart.builder().totalamount(0.0).user(pushUser).build());
		List<UserParameterDetails> userDetails = new ArrayList<>(pushUser.getUserParameterDetails());
		userDetails.add(UserParameterDetails.builder().parameterid(103).srno(1).description("User UniqueId")
				.value(UUID.randomUUID().toString()).build());
		pushUser.setUserParameterDetails(userDetails);
		pushUser.getUserParameterDetails().forEach(parameter -> parameter.setUser(pushUser));

		EndUser saveduser = enduserRepository.save(pushUser);
		EndUserDTO savedDto = userMapper.convertToUserDTO(saveduser);

		String strBody = WELCOME_MAIL_BODY.replace("$$USERNAME$$", savedDto.getUsername());
		String strSubject = WELCOME_MAIL_SUBJECT;

		Boolean mailStatus = sendEmail(savedDto, strSubject, strBody);
		logger.info("Mail status :: " + mailStatus);
		updateCachedData(pushUserDTO);
		return List.of(savedDto);
	}

	@Override
	public List<EndUserDTO> editUser(EndUserDTO putUserDTO) {
		ErrorStatusDetails error = putUserDTO.validateEndUserRequest(putUserDTO, "edit");
		if (error != null) {
			throw new CustomException(error.status(), "BAD_REQUEST", error.statusdescription());
		}
		if (putUserDTO.getUserid() <= 0) {
			throw new CustomException(207, "BAD_REQUEST", "UserId not Found");
		}

		EndUser putUser = userMapper.convertToUserEntity(putUserDTO);
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
		updateCachedData(putUserDTO);
		return List.of(updatedDto);
	}

	@Override
	public ArrayList<Map<String, Object>> getAllUsersNative(Long nUserid) {
		if (nUserid <= 0) {
			throw new CustomException(207, "BAD_REQUEST", "UserId not Found");
		}

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
	public List<EndUserDTO> getAllUsers() {
		String cacheKey = GET_ALL_USERS;
		String cachedUsers = redis.get(cacheKey);
		List<EndUserDTO> usersList;
		if (cachedUsers != null && !cachedUsers.isEmpty()) {
			logger.info("getAllUsers : Fetching All Userdetails from cache");
			usersList = redis.convertStringToObjType(cachedUsers, new TypeToken<List<EndUserDTO>>() {
			});
		} else {
			logger.info("getAllUsers : Fetching All Userdetails from database");
			List<EndUser> users = enduserRepository.findAll().stream()
					.filter((user) -> user.getIsdelete() == 0 && user.getIsactive() == 1).collect(Collectors.toList());
			usersList = users.stream().map(userMapper::convertToUserDTO).collect(Collectors.toList());
			redis.setWithDefaultExpiration(cacheKey, usersList);
		}
		return usersList;
	}

	@Override
	public List<String> disableUser(Long id) {
		try {
			if (id <= 0) {
				throw new CustomException(207, "BAD_REQUEST", "UserId not Found");
			}

			EndUser retrivedUser = getSingleUserByUserId(id);
			retrivedUser.setIsactive(0);
			retrivedUser.setIsdelete(1);
			retrivedUser.setUserlastupdatedate(sf.format(new Date()));
			EndUser savedUser = enduserRepository.save(retrivedUser);
			redis.delete(USER_REDIS_KEY + savedUser.getId());
			redis.delete(GET_ALL_USERS);
			return List.of("User with UserId " + id + " is deleted");
		} catch (Exception e) {
			logger.error("Unexpected error during disableUser", e);
			throw e;
		}
	}

	@Override
	public List<EndUserDTO> getUserById(Long id) {
		return List.of(userMapper.convertToUserDTO(getSingleUserByUserId(id)));
	}

	public boolean checkUserEmailExists(EndUser pushUser) {
		try {
			Optional<EndUser> existingUser = enduserRepository.findByEmail(pushUser.getEmail());
			if (existingUser.isPresent()) {
				if (existingUser.get().getIsdelete() == 0) {
					logger.warn("checkUserEmailExists : User with Email {} already exists", pushUser.getEmail());
					throw new CustomException(208, "BAD_REQUEST",
							"User with Email " + pushUser.getEmail() + " already exists");
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
		String cachedUser = redis.get(USER_REDIS_KEY + id);
		EndUser user = new EndUser();
		if (cachedUser != null && !cachedUser.isEmpty()) {
			EndUserDTO userdto = redis.convertStringToObj(cachedUser, EndUserDTO.class);
			user = userMapper.convertToUserEntity(userdto);
			logger.info("getSingleUserByUserId : cachedUser :: " + userdto);
		} else {
			user = enduserRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + id));
			if (user.getIsactive() == 0 && user.getIsdelete() == 1) {
				throw new CustomException(209, "BAD_REQUEST", "User with Userid " + id + " does not  exists");
			}
			logger.info("getSingleUserByUserId : Retriving userdetails from Database");
			redis.set(USER_REDIS_KEY + user.getId(), userMapper.convertToUserDTO(user));
		}
		return user;
	}

	public void updateCachedData(EndUserDTO user) {
		redis.delete(USER_REDIS_KEY + user.getUserid());
		redis.delete(GET_ALL_USERS);
		redis.set(USER_REDIS_KEY + user.getUserid(), user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		EndUser user = enduserRepository.findByUsername(username).or(() -> enduserRepository.findByEmail(username))
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
		ArrayList<Map<String, Object>> roles = getAllUsersNative(user.getId());
		List<String> rolesList = roles.stream().map(roleMap -> (String) roleMap.get("role")).toList();
		String[] arrRoles = rolesList.toArray(new String[0]);
		return User.builder().username(user.getEmail()).password(user.getPassword()).roles(arrRoles).build();
	}

	public UserDetails getUserDetails(String username) throws UsernameNotFoundException {
		UserDetails user = loadUserByUsername(username);
		return user;
	}

	public boolean requestOtp(String strUserEmail) {
		try {
			EndUser user = enduserRepository.findByEmail(strUserEmail).orElseThrow(() -> new CustomException(301,
					"BAD_REQUEST", "User with Email " + strUserEmail + " does not exist"));

			String otp = String.format("%06d", new Random().nextInt(999999));
			EndUserDTO userDto = userMapper.convertToUserDTO(user);
			String strOtpBody = OTP_BODY.replace("$$OTP$$", otp).replace("$$USERNAME$$", userDto.getUsername());
			Boolean isMailsend = sendEmail(userDto, OTP_SUBJECT, strOtpBody);
			redis.setWithExpiration("USER_LOGINOTP#" + user.getEmail(), otp, 10, TimeUnit.MINUTES);
			logger.info("Generated Otp Shared on " + strUserEmail + " || OTP :: " + otp);
			return isMailsend;
		} catch (Exception e) {
			throw e;
		}
	}

	public Boolean sendEmail(EndUserDTO pushEmail, String strSubject, String strBody) {
		try {
			Email email = new Email();
			email.setToAddresses(pushEmail.getEmail());
			email.setBody(strBody);
			email.setSubject(strSubject);
			email.setOrganisationName("Quick Carts");
			EmailService mail = new EmailService();
			HashMap<String, Object> mailResponse = mail.pushEmail(email, null);
			if (mailResponse.containsKey("error")) {
				return false;
			} else if (mailResponse.containsKey("success")) {
				return true;
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

}
