package com.jodo.portal.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jodo.portal.controller.LoginController.ErrorStatusDetails;
import com.jodo.portal.model.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EndUserDTO {
	private long userid;

	private String username;
	private String password;
	private String designation;
	private String email;
	private String contact;
	private String dateofbirth;
	@NotBlank(message = "Profile image is mandatory")
	private String profileimage;
	private List<RessidentialAddressDTO> address;
	private List<UserParameterDetailsDTO> userParameterDetails;
	private Set<Role> roles = new HashSet<>();

	public ErrorStatusDetails validateEndUserRequest(EndUserDTO endUserDTO) {
		if (endUserDTO.getUsername() == null || endUserDTO.getUsername().isEmpty()) {
			return new ErrorStatusDetails(201, "Username is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getPassword() == null || endUserDTO.getPassword().isEmpty()) {
			return new ErrorStatusDetails(202, "Password is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getDesignation() == null || endUserDTO.getDesignation().isEmpty()) {
			return new ErrorStatusDetails(203, "Designation is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getEmail() == null || endUserDTO.getEmail().isEmpty()) {
			return new ErrorStatusDetails(204, "Email is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getContact() == null || endUserDTO.getContact().isEmpty()) {
			return new ErrorStatusDetails(205, "Contact number is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getDateofbirth() == null || endUserDTO.getDateofbirth().isEmpty()) {
			return new ErrorStatusDetails(206, "Date of birth is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getProfileimage() == null || endUserDTO.getProfileimage().isEmpty()) {
			return new ErrorStatusDetails(207, "Profile image is mandatory", HttpStatus.BAD_REQUEST);
		}
		if (endUserDTO.getAddress() == null || endUserDTO.getAddress().isEmpty()) {
			return new ErrorStatusDetails(208, "Address list cannot be empty", HttpStatus.BAD_REQUEST);
		}
		return null;
	}

}
