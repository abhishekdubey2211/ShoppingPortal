package com.jodo.portal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
	private Long id;
	private double price;
	private int quantity;
	private Long productid;
	@JsonIgnore
	private ProductDTO product;
}