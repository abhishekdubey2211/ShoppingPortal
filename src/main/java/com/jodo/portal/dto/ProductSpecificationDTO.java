package com.jodo.portal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jodo.portal.model.ProductSpecificationDetails;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProductSpecificationDTO {
    private int fieldid;    
    
//    @JsonIgnore
    private int srno;

    @NotBlank(message = "fieldname cannot be blank")
    @Size(max = 500, message = "fieldname cannot exceed 500 characters")
    private String fieldname;
    

    @NotBlank(message = "fieldvalue cannot be blank")
    @Size(max = 255, message = "fieldvalue cannot exceed 255 characters")
    private String fieldvalue;

}
