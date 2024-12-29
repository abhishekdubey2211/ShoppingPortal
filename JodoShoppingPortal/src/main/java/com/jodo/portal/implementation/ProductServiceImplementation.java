package com.jodo.portal.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.jodo.portal.dto.ProductDTO;
import com.jodo.portal.dto.ProductMapper;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.Product;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.ProductRepository;

@Service
public class ProductServiceImplementation implements com.jodo.portal.service.ProductService {
	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplementation.class);
	private final ProductMapper productMapper = new ProductMapper();

	@Autowired
	ProductRepository productRepository;

	@Autowired
	private RedisUtil redis;

	@Override
	public ProductDTO addProduct(ProductDTO pushProductDTO) {
		Product pushProduct = productMapper.convertToProduct(pushProductDTO);
		pushProduct.setInstock(pushProduct.getQuantity());
		pushProduct.setPrice(Math
				.round((pushProduct.getMarketprice() - (pushProduct.getDiscount() * pushProduct.getMarketprice()) / 100)
						* 100.0)
				/ 100.0);
		if (pushProduct.getInstock() > 0) {
			pushProduct.setProductstatus("IN-STOCK");
		} else {
			pushProduct.setProductstatus("OUT-OFF-STOCK");
		}
		pushProduct.setActive(1);
		pushProduct.setIsdelete(0);
		pushProduct.setLast_refilled_date(sf.format(new Date()));
		pushProduct.getImages().forEach(i -> i.setProduct(pushProduct));
		pushProduct.getProductspecification()
				.forEach(productspecification -> productspecification.setProduct(pushProduct));
		Product savedProduct = productRepository.save(pushProduct);
		ProductDTO dto = productMapper.convertToProductDTO(savedProduct);
		redis.setWithDefaultExpiration("PRODUCT#" + dto.getId(), dto);
		return dto;
	}

	@Override
	public ProductDTO editProduct(ProductDTO updatedProductDTO) {
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
			existingProduct.setProductstatus("IN-STOCK");
		} else {
			existingProduct.setProductstatus("OUT-OFF-STOCK");
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
		existingProduct.setIsdelete(0);
		existingProduct.setActive(1);
		existingProduct.setLastupdateddate(sf.format(new Date()));
		existingProduct.setLast_refilled_date(sf.format(new Date()));
		Product updatedProduct = productRepository.save(existingProduct);
		ProductDTO dto = productMapper.convertToProductDTO(updatedProduct);
		redis.setWithDefaultExpiration("PRODUCT#" + dto.getId(), dto);
		return dto;
	}

	@Override
	public ProductDTO getProductById(Long id) {
		try {
			Product retrivedProduct = getSingleProductById(id);
			return productMapper.convertToProductDTO(retrivedProduct);
		} catch (Exception e) {
			logger.error("Unexpected error occured while getProductById ", e);
			throw e;
		}
	}

	@Override
	public List<Object> getProductByType(String type) {
		try {
			List<Product> products = productRepository.findAll();
			List<Object> activeProducts = products.stream()
					.filter(product -> product.getCategory().equalsIgnoreCase(type) && product.getInstock() > 0
							&& product.getIsdelete() != 1)
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
			product = productRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Product with ProductId " + id + " not found"));
			if (product.getIsdelete() == 1) {
				throw new ResourceNotFoundException("Product with ProductId " + id + " not found");
			}
			return product;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getSingleProductById ", e);
			throw e;
		}
	}

	@Override
	public ProductDTO addProductQuantity(Long productid, int productQuantity) {
		try {
			Product existingProduct = productRepository.findById(productid).orElseThrow(
					() -> new ResourceNotFoundException("Product with ProductId " + productid + " not found"));
			if (existingProduct.getIsdelete() == 1) {
				throw new ResourceNotFoundException("Product with ProductId " + productid + " not found");
			}
			existingProduct.setQuantity(existingProduct.getInstock() + productQuantity);
			existingProduct.setInstock(existingProduct.getQuantity());
			Product savedProduct = productRepository.save(existingProduct);
			return productMapper.convertToProductDTO(savedProduct);
		} catch (Exception e) {
			logger.error("Unexpected error occured while getSingleProductById ", e);
			throw e;
		}
	}

	@Override
	public List<Object> getAllProducts() {
		try {
			List<Product> products = new ArrayList<>();
			products = productRepository.findAll();
			List<Object> productList = products.stream().filter(p -> p.getIsdelete() == 0 && p.getActive() == 1)
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public Page<Object> getAllProducts(int page, int size) {
	    try {
	        // Create a Pageable object with page number and size
	        Pageable pageable = PageRequest.of(page, size);
	        
	        // Get paginated products
	        Page<Product> productPage = productRepository.findAll(pageable);

	        // Filter and map to DTO while maintaining pagination
	        Page<Object> productList = productPage.map(product -> {
	            if (product.getIsdelete() == 0 && product.getActive() == 1) {
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
	public List<Object> getAllInStocksProduct() {
		try {
			List<Product> products = new ArrayList<>();
			products = productRepository.findAll().stream()
					.filter(p -> p.getIsdelete() == 0 || p.getProductstatus().equalsIgnoreCase("IN-STOCK"))
					.collect(Collectors.toList());
			List<Object> productList = products.stream()
					.filter(p -> p.getIsdelete() == 0 && p.getActive() == 1 && p.getQuantity() > 0)
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public List<Object> getAllOutOfStockProducts() {
		try {
			List<Object> productList = productRepository.findAll().stream()
					.filter(p -> p.getQuantity() <= 0 && p.getIsdelete() == 0
							&& p.getProductstatus().equalsIgnoreCase("OUT-OFF-STOCK"))
					.map(product -> productMapper.convertToProductDTO(product)).collect(Collectors.toList());
			return productList;
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public List<Object> disableProduct(Long id) {
		try {
			Product retrivedProduct = getSingleProductById(id);
			retrivedProduct.setActive(0);
			retrivedProduct.setIsdelete(1);
			retrivedProduct.setLastupdateddate(sf.format(new Date()));
			productRepository.save(retrivedProduct);
			return List.of("Poduct with ProductId " + id + " is disable done Sucessfully......");
		} catch (Exception e) {
			logger.error("Unexpected error occured while getAllProducts ", e);
			throw e;
		}
	}

	@Override
	public List<Object> getProductSummary() {
		List<Product> products = productRepository.findAll();
		List<Product> productList = products.stream().filter(p -> p.getIsdelete() == 0 && p.getActive() == 1)
				.collect(Collectors.toList());
		List<Object> productSummaryArray = new ArrayList<>() ;
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
