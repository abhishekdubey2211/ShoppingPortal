package com.jodo.portal.dto;

import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.RessidentialAddress;
import com.jodo.portal.model.UserParameterDetails;

import java.util.ArrayList;

public class EndUserMapper {

    public EndUserDTO convertToUserDTO(EndUser endUser) {
        EndUserDTO dto = new EndUserDTO();
        dto.setUserid(endUser.getId());
        dto.setUsername(endUser.getUsername());
        dto.setContact(endUser.getContact());
        dto.setDesignation(endUser.getDesignation());
        dto.setEmail(endUser.getEmail());  
        dto.setDateofbirth(endUser.getDateofbirth());
        dto.setProfileimage(endUser.getProfileimage());
//        dto.setRole(endUser.getRole());
        dto.setConfirmpassword(endUser.getPassword());
        dto.setPassword(endUser.getPassword());
        dto.setAddress(endUser.getAddress() != null ? 
            endUser.getAddress().stream().map(this::convertToAddressDTO).toList() : new ArrayList<>());
        dto.setUserParameterDetails(endUser.getUserParameterDetails() != null ? 
            endUser.getUserParameterDetails().stream().map(this::convertToUserParameterDetailsDTO).toList() : new ArrayList<>());
        dto.setRoles(endUser.getRoles());
        return dto;
    }
    
    
    public EndUser convertToUserEntity(EndUserDTO endUserDTO) {
        EndUser endUser = new EndUser();
        endUser.setId(endUserDTO.getUserid());
        endUser.setUsername(endUserDTO.getUsername());
        endUser.setPassword(endUserDTO.getConfirmpassword());
        endUser.setDesignation(endUserDTO.getDesignation());
        endUser.setEmail(endUserDTO.getEmail());
        endUser.setDateofbirth(endUserDTO.getDateofbirth());
        endUser.setContact(endUserDTO.getContact());
        endUser.setProfileimage(endUserDTO.getProfileimage());
        //endUser.setRole(endUserDTO.getRole());
        // Null safety check
        endUser.setAddress(endUserDTO.getAddress() != null ? 
            endUserDTO.getAddress().stream().map(this::convertToAddressEntity).toList() : new ArrayList<>());
        endUser.setUserParameterDetails(endUserDTO.getUserParameterDetails() != null ? 
            endUserDTO.getUserParameterDetails().stream().map(this::convertToUserParameterDetails).toList() : new ArrayList<>());
        endUser.setRoles(endUserDTO.getRoles());
        return endUser;
    }

    public RessidentialAddressDTO convertToAddressDTO(RessidentialAddress address) {
        RessidentialAddressDTO dto = new RessidentialAddressDTO();
        dto.setAddressid(address.getId());
        dto.setAddress1(address.getAddress1());
        dto.setAddress2(address.getAddress2());
        dto.setCity(address.getCity());
        dto.setPincode(address.getPincode());
        dto.setState(address.getState());
        dto.setVillage(address.getVillage());
        dto.setIsPrimary(address.getIsPrimary());
        return dto;
    }

    public RessidentialAddress convertToAddressEntity(RessidentialAddressDTO addressDTO) {
        RessidentialAddress address = new RessidentialAddress();
        address.setId(addressDTO.getAddressid());
        address.setAddress1(addressDTO.getAddress1());
        address.setAddress2(addressDTO.getAddress2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPincode(addressDTO.getPincode());
        address.setVillage(addressDTO.getVillage());
        address.setIsPrimary(addressDTO.getIsPrimary());
        return address;
    }

    public UserParameterDetailsDTO convertToUserParameterDetailsDTO(UserParameterDetails parameterDetails) {
        UserParameterDetailsDTO dto = new UserParameterDetailsDTO();
        dto.setParameterid(parameterDetails.getParameterid());
        dto.setDescription(parameterDetails.getDescription());
        dto.setSrno(parameterDetails.getSrno());
        dto.setValue(parameterDetails.getValue());
        return dto;
    }

    public UserParameterDetails convertToUserParameterDetails(UserParameterDetailsDTO parameterDetailsDTO) {
        UserParameterDetails parameterDetails = new UserParameterDetails();
        parameterDetails.setParameterid(parameterDetailsDTO.getParameterid());
        parameterDetails.setSrno(parameterDetailsDTO.getSrno());
        parameterDetails.setDescription(parameterDetailsDTO.getDescription());
        parameterDetails.setValue(parameterDetailsDTO.getValue());
        return parameterDetails;
    }
}
