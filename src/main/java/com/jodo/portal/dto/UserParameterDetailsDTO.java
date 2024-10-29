package com.jodo.portal.dto;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UserParameterDetailsDTO {

    private int parameterid;    
    private int srno;

    @NotBlank(message = "Value cannot be blank")
    @Size(max = 255, message = "Value cannot exceed 255 characters")
    private String value;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
