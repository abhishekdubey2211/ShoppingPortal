package com.jodo.portal.service;

import com.jodo.portal.dto.EndUserDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface EndUserService {
    EndUserDTO addUser(EndUserDTO pushUserDTO, String strRoles);

    EndUserDTO editUser(EndUserDTO putUserDTO);

    ArrayList<Map<String, Object>> getAllUsersNative(Long nUserid);

    List<Object> getAllUsers();

    List<Object> disableUser(Long id);

    EndUserDTO getUserById(Long id);

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    Map<String, String> checkSessionIsActive(String sessionId) throws Exception;

    String generateSessionId(String username) throws Exception;

    String logout(String sessionId) throws Exception;
}
