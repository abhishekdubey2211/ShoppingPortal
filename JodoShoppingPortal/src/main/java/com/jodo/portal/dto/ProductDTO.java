package com.jodo.portal.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;

import com.jodo.portal.model.ProductSpecificationDetails;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotNull(message = "Product name cannot be null")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Brand cannot be null")
    @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
    private String brand;

    @NotNull(message = "Category cannot be null")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;

    @NotNull(message = "Model cannot be null")
    @Size(min = 2, max = 50, message = "Model must be between 2 and 50 characters")
    private String model;

    @NotNull(message = "Marketprice cannot be null")
    @Min(value = 0, message = "Marketprice cannot be less than 0")
    private double marketprice;
    
    @NotNull(message = "Discount cannot be null")
    @Min(value = 0, message = "Discount cannot be less than 0")
    private double price;
    private double discount;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    private List<ProductImagesDTO> productimages;

    @NotNull(message = "Description cannot be null")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;

    private String productstatus;
    
    private List<ProductSpecificationDTO> productspecification;
}
