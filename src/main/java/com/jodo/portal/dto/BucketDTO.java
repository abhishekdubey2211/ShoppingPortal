package com.jodo.portal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jodo.portal.model.Bucket;
import com.jodo.portal.model.Order;
import com.jodo.portal.model.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
public class BucketDTO {
	private Long id;
	private double price;
	private Long productid;
	@JsonIgnore
	private ProductDTO product;
	private int quantity;
	private String productorderstatus;
}
