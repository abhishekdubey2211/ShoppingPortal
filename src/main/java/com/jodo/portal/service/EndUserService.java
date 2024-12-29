package com.jodo.portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jodo.portal.dto.EndUserDTO;

public interface EndUserService {
	
    List<EndUserDTO> addUser(EndUserDTO pushUserDTO, String strRoles);

    List<EndUserDTO> editUser(EndUserDTO putUserDTO);

    ArrayList<Map<String, Object>> getAllUsersNative(Long nUserid);

    List<EndUserDTO> getAllUsers();

    List<String> disableUser(Long id);

    List<EndUserDTO> getUserById(Long id);

}
