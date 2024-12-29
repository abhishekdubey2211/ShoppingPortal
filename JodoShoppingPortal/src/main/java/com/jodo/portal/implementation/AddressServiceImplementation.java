package com.jodo.portal.implementation;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jodo.portal.dto.EndUserMapper;
import com.jodo.portal.dto.RessidentialAddressDTO;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.RessidentialAddress;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.AddressRepository;

@Service
public class AddressServiceImplementation implements com.jodo.portal.service.AddressService {
	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(AddressServiceImplementation.class);

	public EndUserServiceImplementation userServiceImplementation = new EndUserServiceImplementation();
	private final EndUserMapper userMapper = new EndUserMapper();
	@Autowired
	private RedisUtil redis;

	@Autowired
	private AddressRepository addressRepository;

	@Override
	public RessidentialAddressDTO addAddress(EndUser user, RessidentialAddressDTO pushAddressDTO) {
		RessidentialAddress pushAddress = userMapper.convertToAddressEntity(pushAddressDTO);
		EndUser retrivedUser = userServiceImplementation.getSingleUserByUserId(user.getId());
		pushAddress.setUser(retrivedUser);
		RessidentialAddress  savedAddress = addressRepository.save(pushAddress);
		return userMapper.convertToAddressDTO(savedAddress);
	}
	
	public String timepass(){
		return "TIMEPASS";
	}
}
