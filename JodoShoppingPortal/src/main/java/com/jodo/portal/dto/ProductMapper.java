package com.jodo.portal.dto;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.jodo.portal.model.Product;
import com.jodo.portal.model.ProductImages;
import com.jodo.portal.model.ProductSpecificationDetails;
import com.jodo.portal.model.UserParameterDetails;

public class ProductMapper {

	public ProductDTO convertToProductDTO(Product product) {
		ProductDTO dto = new ProductDTO();
		dto.setId(product.getId());
		dto.setName(product.getName());
		dto.setBrand(product.getBrand());
		dto.setCategory(product.getCategory());
		dto.setModel(product.getModel());
		dto.setMarketprice(product.getMarketprice());
		dto.setPrice(product.getPrice());
		dto.setDiscount(product.getDiscount());
		dto.setQuantity(product.getQuantity());
		dto.setDescription(product.getDescription());
		dto.setProductstatus(product.getProductstatus());
		dto.setProductimages(
				product.getImages() != null ? product.getImages().stream().map(this::convertToProductImagesDTO).toList()
						: new ArrayList<>());
		dto.setProductspecification(product.getProductspecification() != null
				? product.getProductspecification().stream().map(this::convertToProductSpecificationDTO).toList()
				: new ArrayList());
		return dto;
	}

	public Product convertToProduct(ProductDTO productDTO) {
		Product product = new Product();
		product.setId(productDTO.getId());
		product.setName(productDTO.getName());
		product.setBrand(productDTO.getBrand());
		product.setCategory(productDTO.getCategory());
		product.setModel(productDTO.getModel());
		product.setMarketprice(productDTO.getMarketprice());
		product.setPrice(productDTO.getPrice());
		product.setDiscount(productDTO.getDiscount());
		product.setQuantity(productDTO.getQuantity());
		product.setDescription(productDTO.getDescription());
		product.setProductstatus(productDTO.getProductstatus());
		product.setImages(productDTO.getProductimages() != null
				? productDTO.getProductimages().stream().map(this::convertToProductImages).toList()
				: new ArrayList<>());
		product.setProductspecification(productDTO.getProductspecification() != null
				? productDTO.getProductspecification().stream().map(this::convertToProductSpecification).toList()
				: new ArrayList<>());
		return product;
	}

	private ProductImagesDTO convertToProductImagesDTO(ProductImages productImages) {
		ProductImagesDTO dto = new ProductImagesDTO();
		dto.setId(productImages.getId());
		dto.setUrl(productImages.getUrl());
		return dto;
	}

	private ProductImages convertToProductImages(ProductImagesDTO productImagesDTO) {
		ProductImages productImages = new ProductImages();
		productImages.setId(productImagesDTO.getId());
		productImages.setUrl(productImagesDTO.getUrl());
		return productImages;
	}

	public ProductSpecificationDTO convertToProductSpecificationDTO(ProductSpecificationDetails productSpecification) {
		ProductSpecificationDTO dto = new ProductSpecificationDTO();
		dto.setFieldid(productSpecification.getFieldid());
		dto.setFieldname(productSpecification.getFieldname());
		dto.setSrno(productSpecification.getSrno());
		dto.setFieldvalue(productSpecification.getFieldvalue());
		return dto;
	}

	public ProductSpecificationDetails convertToProductSpecification(ProductSpecificationDTO productSpecificationDTO) {
		ProductSpecificationDetails productSpecification = new ProductSpecificationDetails();
		productSpecification.setFieldid(productSpecificationDTO.getFieldid());
		productSpecification.setSrno(productSpecificationDTO.getSrno());
		productSpecification.setFieldname(productSpecificationDTO.getFieldname());
		productSpecification.setFieldvalue(productSpecificationDTO.getFieldvalue());
		return productSpecification;
	}
}
