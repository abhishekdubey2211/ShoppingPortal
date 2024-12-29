package com.jodo.portal.service;

import com.jodo.portal.dto.ProductDTO;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductDTO> addProduct(ProductDTO pushProductDTO);

    List<ProductDTO> editProduct(ProductDTO updatedProductDTO);

    List<ProductDTO> getProductById(Long id);

    List<ProductDTO> getProductByType(String type);

    List<ProductDTO> addProductQuantity(Long productid, int productQuantity);

    List<ProductDTO> getAllProducts();

    Page<ProductDTO> getAllProducts(int page, int size);

    List<ProductDTO> getAllInStocksProduct();

    List<ProductDTO> getAllOutOfStockProducts();

    String disableProduct(Long id);

    List<JSONObject> getProductSummary();
}
