package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.implementation.ProductServiceImplementation;


@RestController
@RequestMapping("/api")
public class ProductController {


	@Autowired
	ProductServiceImplementation productService;

	@PostMapping("/v2/product")
	public ResponseEntity<?> pushProduct(@RequestBody ProductDTO product) {
		List<Object> productList = new ArrayList<>();
		ProductDTO savedProduct = productService.addProduct(product);
		productList.add(savedProduct);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.createResponse(1,
				"Product Added Sucessfully with ProductId " + savedProduct.getId(), productList));
	}
	

	@PutMapping("/v2/product")
	public ResponseEntity<?> putProduct(@RequestBody ProductDTO product) {
		List<Object> productList = new ArrayList<>();
		ProductDTO savedProduct = productService.editProduct(product);
		productList.add(savedProduct);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseApi.createResponse(1,
				"Product Updated Sucessfully with ProductId " + savedProduct.getId(), productList));
	}
	

	@GetMapping("/v3/product/get_products")
	public ResponseEntity<?> getAllProduct() {
		List<Object> productList = new ArrayList<>();
		productList = productService.getAllProducts();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All Products Retrived Successfully....", productList));
	}

	

    @GetMapping("/v3/page/get_products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Call the service method with pagination parameters
            Page<Object> productList = productService.getAllProducts(page, size);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseApi.createResponse(1, "All Products Retrieved Successfully", productList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseApi.createResponse(0, "Error retrieving products", null));
        }
    }
	
	@GetMapping("/v3/product/get_products/in_stock")
	public ResponseEntity<?> getAllInStocksProduct() {
		List<Object> productList = new ArrayList<>();
		productList = productService.getAllInStocksProduct();
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All In Stock Products Retrived Successfully....", productList));
	}

	@GetMapping("/v2/product/get_products/outoff_stock")
	public ResponseEntity<?> getAllOutOffStocksProduct() {
		List<Object> productList = new ArrayList<>();
		productList = productService.getAllOutOfStockProducts();
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "All Out off Socks Products Retrived Successfully....", productList));
	}

	@GetMapping("/v3/product/get_products/{categoryname}")
	public ResponseEntity<?> getProductsByCategoryname(@PathVariable("categoryname") String categoryname) {
		List<Object> productList = new ArrayList<>();
		productList = productService.getProductByType(categoryname);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseApi.createResponse(1,
				"All " + categoryname + " Products Retrived Successfully....", productList));
	}

	@DeleteMapping("/v2/product/{productId}")
	public ResponseEntity<?> disaleProduct(@PathVariable("productId") Long productId) {
		List<Object> productList = new ArrayList<>();
		productList = productService.disableProduct(productId);
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "Product " + productId + " disable done Successfully....", productList));
	}

	@GetMapping("/v3/product/{productId}")
	public ResponseEntity<?> getProductById(@PathVariable("productId") Long productId) {
		List<Object> productList = new ArrayList<>();
		ProductDTO product = productService.getProductById(productId);
		productList.add(product);
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "Product " + productId + "disable done Successfully....", productList));
	}


	@PutMapping("/v2/product/{productId}/quantity/{quantity}")
	public ResponseEntity<?> getProductById(@PathVariable("productId") Long productId,@PathVariable("quantity") int quantity) {
		List<Object> productList = new ArrayList<>();
		ProductDTO product = productService.addProductQuantity(productId,quantity);
		productList.add(product);
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "Product " + productId + "disable done Successfully....", productList));
	}
	
	@GetMapping("/v2/product/summary")
	public ResponseEntity<?> getProductSummary() {
		List<Object> productList = productService.getProductSummary();
		return ResponseEntity.status(HttpStatus.OK).body(
				ResponseApi.createResponse(1, "Product summary details retrived Successfully....", productList));
	}
	
	

}
