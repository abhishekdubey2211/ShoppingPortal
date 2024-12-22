package com.jodo.portal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jodo.portal.model.Product;
import com.jodo.portal.model.ProductImages;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class ProductImagesDTO {

	@Id
	private long id;

	@Column(name = "url")
	private String url;
}