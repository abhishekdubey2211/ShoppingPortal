package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jodo.portal.dto.ProductDTO;
import com.jodo.portal.model.FilterCriteria;
import com.jodo.portal.model.Product;
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.repository.ProductRepository;
import com.jodo.portal.implementation.EndUserServiceImplementation;
import com.jodo.portal.implementation.MyEntitySpecification;
import com.jodo.portal.implementation.ProductServiceImplementation;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	ProductServiceImplementation productService;

	@Autowired
	ProductRepository productRepository;

	@PostMapping("/product")
	public ResponseEntity<?> pushProduct(@RequestBody ProductDTO product) {
		List<ProductDTO> productList = productService.addProduct(product);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "Product Added Sucessfully", productList));
	}

	@PutMapping("/product")
	public ResponseEntity<?> putProduct(@RequestBody ProductDTO product) {
		List<ProductDTO> productList = productService.editProduct(product);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(ResponseApi.createResponse(1, "Product Updated Sucessfully", productList));
	}

	@GetMapping("/product/get_products")
	public ResponseEntity<?> getAllProduct() {
		List<ProductDTO> productList = productService.getAllProducts();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All Products Retrived Successfully", productList));
	}

	@GetMapping("/page/get_products")
	public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Page<ProductDTO> productList = productService.getAllProducts(page, size);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All Products Retrieved Successfully", productList));
	}

	@GetMapping("/product/get_products/in_stock")
	public ResponseEntity<?> getAllInStocksProduct() {
		List<ProductDTO> productList = productService.getAllInStocksProduct();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All In Stock Products Retrived Successfully", productList));
	}

	@GetMapping("/product/get_products/outoff_stock")
	public ResponseEntity<?> getAllOutOffStocksProduct() {
		List<ProductDTO> productList = productService.getAllOutOfStockProducts();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All Out off Socks Products Retrived Successfully", productList));
	}

	@GetMapping("/product/get_products/{categoryname}")
	public ResponseEntity<?> getProductsByCategoryname(@PathVariable("categoryname") String categoryname) {
		List<ProductDTO> productList = productService.getProductByType(categoryname);
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "All " + categoryname + " Products Retrived Successfully", productList));
	}

	@GetMapping("/product/brand/{brandname}/{pageno}/{size}")
	public ResponseEntity<?> getProductsByBrand(@PathVariable("brandname") String strBrand,
			@PathVariable("pageno") int pageNo, @PathVariable("size") int size) {
		Map<String, Object> brandList = productService.getProductsByBrand(strBrand, pageNo, size);
		Object response = ResponseApi.createResponse(1, "Product Retrived Successfully", brandList);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping("/product/{productId}")
	public ResponseEntity<?> disaleProduct(@PathVariable("productId") Long productId) {
		String productdata = productService.disableProduct(productId);
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "Product " + productId + " disable done Successfully.", productdata));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<?> getProductById(@PathVariable("productId") Long productId) {
		List<ProductDTO> productList = productService.getProductById(productId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "Product " + productId + "disable done Successfully", productList));
	}

	@PutMapping("/product/{productId}/quantity/{quantity}")
	public ResponseEntity<?> getProductById(@PathVariable("productId") Long productId,
			@PathVariable("quantity") int quantity) {
		List<ProductDTO> productList = productService.addProductQuantity(productId, quantity);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "Product " + productId + "disable done Successfully", productList));
	}

	@GetMapping("/product/summary")
	public ResponseEntity<?> getProductSummary() {
		List<JSONObject> productList = productService.getProductSummary();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "Product summary details retrived Successfully", productList));
	}

	@GetMapping("/filter-products")
	public ResponseEntity<?> getFilteredAndSortedEntities(@RequestBody ProductRequestDTO requestDTO) {
		int page = requestDTO.getPage();
		int size = requestDTO.getSize();
		String sortBy = requestDTO.getSortBy();
		String sortDir = requestDTO.getSortDir();
		List<FilterCriteria> filterCriteriaList = requestDTO.getFilterCriteriaList();

		Specification<Product> spec = MyEntitySpecification.getFilteredSpec(filterCriteriaList);
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Product> productPage = productRepository.findAll(spec, pageable);
		Map<String, Object> response = new LinkedHashMap<>();
		
		response.put("content", productPage.getContent()); 
		response.put("pageNo", productPage.getNumber());
		response.put("pageSize", productPage.getSize());
		response.put("totalElements", productPage.getTotalElements());
		response.put("totalPages", productPage.getTotalPages());
		response.put("last", productPage.isLast());
		response.put("sortDetails",
				pageable.getSort().stream()
						.map(order -> Map.of("property", order.getProperty(), "direction", order.getDirection().name()))
						.collect(Collectors.toList()));
		response.put("filterCriteria", filterCriteriaList);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "Product Filtered Data retrived Successfully", List.of(response)));
		}

}
