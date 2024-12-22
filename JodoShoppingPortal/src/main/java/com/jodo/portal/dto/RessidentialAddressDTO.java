package com.jodo.portal.dto;

import java.util.List;
import java.util.Set;

import com.jodo.portal.model.Role;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RessidentialAddressDTO {

    private long addressid;

    @NotBlank(message = "Address line 1 cannot be blank")
    @Size(max = 200, message = "Address line 1 cannot exceed 200 characters")
    private String address1;

    private String address2;
    private String village;

    @NotBlank(message = "State cannot be blank")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "Pincode cannot be blank")
    @Size(min = 6, max = 6, message = "Pincode must be exactly 6 characters")
    private String pincode;

    private int isPrimary;
}
