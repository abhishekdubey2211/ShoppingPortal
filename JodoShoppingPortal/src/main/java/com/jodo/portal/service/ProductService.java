package com.jodo.portal.service;

import com.jodo.portal.dto.ProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductDTO addProduct(ProductDTO pushProductDTO);

    ProductDTO editProduct(ProductDTO updatedProductDTO);

    ProductDTO getProductById(Long id);

    List<Object> getProductByType(String type);

    ProductDTO addProductQuantity(Long productid, int productQuantity);

    List<Object> getAllProducts();

    Page<Object> getAllProducts(int page, int size);

    List<Object> getAllInStocksProduct();

    List<Object> getAllOutOfStockProducts();

    List<Object> disableProduct(Long id);

    List<Object> getProductSummary();
}
