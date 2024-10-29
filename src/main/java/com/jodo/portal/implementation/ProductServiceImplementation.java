package com.jodo.portal.implementation;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodo.portal.constants.MessageConstants;
import com.jodo.portal.dto.ProductDTO;
import com.jodo.portal.dto.ProductMapper;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.Product;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.ProductRepository;
import com.jodo.portal.service.ProductService;

import io.jsonwebtoken.lang.Arrays;

@Service
public class ProductServiceImplementation implements ProductService, MessageConstants {
	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplementation.class);
	private final ProductMapper productMapper = new ProductMapper();

	@Autowired
	ProductRepository productRepository;

	@Autowired
	private RedisUtil redis;

	@Override
	public List<ProductDTO> addProduct(ProductDTO pushProductDTO) {
		List<ProductDTO> productList = new ArrayList<>();
		Product pushProduct = productMapper.convertToProduct(pushProductDTO);
		pushProduct.setInstock(pushProduct.getQuantity());
		pushProduct.setPrice(Math
				.round((pushProduct.getMarketprice() - (pushProduct.getDiscount() * pushProduct.getMarketprice()) / 100)
						* 100.0) / 100.0);
		if (pushProduct.getInstock() > 0) {
			pushProduct.setProductstatus(PRODUCT_INSTOCK);
		} else {
			pushProduct.setProductstatus(PRODUCT_OUTOFFSTOCK);
		}
		pushProduct.setActive(ACTIVE);
		pushProduct.setIsdelete(INACTIVE);
		pushProduct.setLast_refilled_date(sf.format(new Date()));
		pushProduct.getImages().forEach(i -> i.setProduct(pushProduct));
		pushProduct.getProductspecification()
				.forEach(productspecification -> productspecification.setProduct(pushProduct));
		Product savedProduct = productRepository.save(pushProduct);
		ProductDTO dto = productMapper.convertToProductDTO(savedProduct);
		redis.setWithDefaultExpiration(PRODUCT_REDISKEY + dto.getId(), dto);
		productList.add(dto);
		return productList;
	}

	@Override
	public List<ProductDTO> editProduct(ProductDTO updatedProductDTO) {

		List<ProductDTO> productList = new ArrayList<>();

		Product putProduct = productMapper.convertToProduct(updatedProductDTO);

		Product existingProduct = productRepository.findById(putProduct.getId())
				.orElseThrow(() -> new RuntimeException("Product not found with ID: " + putProduct.getId()));

		existingProduct.setName(putProduct.getName());
		existingProduct.setBrand(putProduct.getBrand());
		existingProduct.setCategory(putProduct.getCategory());
		existingProduct.setModel(putProduct.getModel());
		existingProduct.setMarketprice(putProduct.getMarketprice());
		existingProduct.setPrice(Math.round(
				(putProduct.getMarketprice() - (putProduct.getDiscount() * putProduct.getMarketprice()) / 100) * 100.0)
				/ 100.0);
		existingProduct.setQuantity(putProduct.getQuantity());
		existingProduct.setDescription(putProduct.getDescription());
		existingProduct.setInstock(putProduct.getQuantity());
		if (existingProduct.getQuantity() > 0) {
			existingProduct.setProductstatus(PRODUCT_INSTOCK);
		} else {
			existingProduct.setProductstatus(PRODUCT_OUTOFFSTOCK);
		}
		existingProduct.getImages().clear();
		existingProduct.getProductspecification().clear();

		putProduct.getImages().forEach(i -> {
			i.setProduct(putProduct);
			existingProduct.getImages().add(i);
		});
		putProduct.getProductspecification().forEach(productspecification -> {
			productspecification.setProduct(putProduct);
			existingProduct.getProductspecification().add(productspecification);
		});
		existingProduct.setIsdelete(INACTIVE);
		existingProduct.setActive(ACTIVE);
		existingProduct.setLastupdateddate(sf.format(new Date()));
		existingProduct.setLast_refilled_date(sf.format(new Date()));
		Product updatedProduct = productRepository.save(existingProduct);
		ProductDTO dto = productMapper.convertToProductDTO(updatedProduct);
		redis.delete(PRODUCT_REDISKEY + dto.getId());
		redis.setWithDefaultExpiration(PRODUCT_REDISKEY + dto.getId(), dto);
		productList.add(dto);
		return productList;
	}

	public Map<String, Object> getProductsByBrand(String strBrand, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "discount"));
		Page<Product> content = productRepository.findByBrand(strBrand, pageable);
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", content.getContent());

		response.put("pageNumber", content.getNumber());
		response.put("pageSize", content.getSize());
		response.put("totalItems", content.getTotalElements());
		response.put("totalPages", content.getTotalPages());
		Map<String, Object> sortDetails = new LinkedHashMap<>();
		sortDetails.put("sortedBy", "discount");
		sortDetails.put("sortDirection", "DESC");
		response.put("sort", sortDetails);
		return response;
	}

	@Override
	public List<ProductDTO> getProductById(Long id) {
		try {
			Product retrivedProduct = getSingleProductById(id);
			return List.of(productMapper.convertToProductDTO(retrivedProduct));
		} catch (Exception e) {
			logger.error("Unexpected error occured while getProductById ", e);
			throw e;
		}
	}

	@Override
	public List<ProductDTO> getProductByType(String type) {
		try {
			List<Product> products = productRepository.findAll();
			List<ProductDTO> activeProducts = products.stream()
					.filter(product -> product.getCategory().equalsIgnoreCase(type) && product.getInstock() > 0
							&& product.getIsdelete() != ACTIVE)
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return activeProducts;
		} catch (Exception e) {
			logger.error("Unexpected error occurred while getting products by type", e);
			throw e;
		}
	}

	public Product getSingleProductById(Long id) {
		try {
			Product product = new Product();
			String strProductCache = redis.get(PRODUCT_REDISKEY + id);
			if (strProductCache != null && !strProductCache.isEmpty()) {
				logger.info("getSingleProductById : Product Retrived from Redis");
				ProductDTO dto = redis.convertStringToObj(strProductCache, ProductDTO.class);
				product = productMapper.convertToProduct(dto);
			} else {
				product = productRepository.findById(id).orElseThrow(
						() -> new ResourceNotFoundException("Product with ProductId " + id + " not found"));
				if (product.getIsdelete() == 1) {
					throw new ResourceNotFoundException("Product with ProductId " + id + " not found");
				}
				logger.info("getSingleProductById : Product Retrived from Database");
				redis.setWithDefaultExpiration(PRODUCT_REDISKEY + id, productMapper.convertToProductDTO(product));
			}
			return product;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getSingleProductById ", e);
			throw e;
		}
	}

	@Override
	public List<ProductDTO> addProductQuantity(Long productid, int productQuantity) {
		try {
			Product existingProduct = productRepository.findById(productid).orElseThrow(
					() -> new ResourceNotFoundException("Product with ProductId " + productid + " not found"));
			if (existingProduct.getIsdelete() == ACTIVE) {
				throw new ResourceNotFoundException("Product with ProductId " + productid + " not found");
			}
			existingProduct.setQuantity(existingProduct.getInstock() + productQuantity);
			existingProduct.setInstock(existingProduct.getQuantity());
			Product savedProduct = productRepository.save(existingProduct);
			return List.of( productMapper.convertToProductDTO(savedProduct));
		} catch (Exception e) {
			logger.error("Unexpected error occured while getSingleProductById ", e);
			throw e;
		}
	}

	@Override
	public List<ProductDTO> getAllProducts() {
		try {
			List<Product> products = productRepository.findAll();

			List<ProductDTO> productList = products.stream()
					.filter(p -> p.getIsdelete() == INACTIVE && p.getActive() == ACTIVE)
					.sorted(Comparator.comparing(Product::getId).reversed()) // Sort by ID in descending order
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());

			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occurred while getting all products", e);
			throw e;
		}
	}

	@Override
	public Page<ProductDTO> getAllProducts(int page, int size) {
		try {
			// Create a Pageable object with page number and size
			Pageable pageable = PageRequest.of(page, size);

			// Get paginated products
			Page<Product> productPage = productRepository.findAll(pageable);

			// Filter and map to DTO while maintaining pagination
			Page<ProductDTO> productList = productPage.map(product -> {
				if (product.getIsdelete() == INACTIVE && product.getActive() == ACTIVE) {
					return productMapper.convertToProductDTO(product);
				}
				return null;
			}); // Filter out nulls

			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occurred while getting all products with pagination", e);
			throw e;
		}
	}

	@Override
	public List<ProductDTO>  getAllInStocksProduct() {
		try {
			List<Product> products = new ArrayList<>();
			products = productRepository.findAll().stream()
					.filter(p -> p.getIsdelete() == 0 || p.getProductstatus().equalsIgnoreCase(PRODUCT_INSTOCK))
					.collect(Collectors.toList());
			List<ProductDTO> productList = products.stream()
					.filter(p -> p.getIsdelete() == INACTIVE && p.getActive() == ACTIVE && p.getQuantity() > 0)
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public List<ProductDTO> getAllOutOfStockProducts() {
		try {
			List<ProductDTO> productList = productRepository.findAll().stream()
					.filter(p -> p.getQuantity() <= 0 && p.getIsdelete() == INACTIVE
							&& p.getProductstatus().equalsIgnoreCase(PRODUCT_OUTOFFSTOCK))
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public  String disableProduct(Long id) {
		try {
			Product retrivedProduct = getSingleProductById(id);
			retrivedProduct.setActive(INACTIVE);
			retrivedProduct.setIsdelete(ACTIVE);
			retrivedProduct.setLastupdateddate(sf.format(new Date()));
			productRepository.save(retrivedProduct);
			return "Poduct with ProductId " + id + " is disable done Sucessfully......";
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public List<JSONObject> getProductSummary() {
		List<Product> products = productRepository.findAll();
		List<Product> productList = products.stream()
				.filter(p -> p.getIsdelete() == INACTIVE && p.getActive() == ACTIVE).collect(Collectors.toList());
		List<JSONObject> productSummaryArray = new ArrayList<>();
		productList.forEach(product -> {
			JSONObject productSummary = new JSONObject();
			productSummary.put("productId", product.getId());
			productSummary.put("productName", product.getName());
			productSummary.put("brandName", product.getBrand());
			productSummary.put("stockQuantity", product.getInstock());
			productSummary.put("status", product.getProductstatus());
			productSummaryArray.add(productSummary);
		});
		return productSummaryArray;
	}
}
