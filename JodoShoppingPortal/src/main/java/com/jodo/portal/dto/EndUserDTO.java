package com.jodo.portal.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @NotBlank(message = "Profile image is mandatory")
    private String profileimage;

//    private String role;

    private List<RessidentialAddressDTO> address;
    private List<UserParameterDetailsDTO> userParameterDetails;
    
    private Set<Role> roles = new HashSet<>();
}
