package com.jodo.portal.service;

import com.jodo.portal.dto.CartDTO;
import com.jodo.portal.dto.CartItemDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
	
    List<CartDTO> addProductToCart(Long userid, CartItemDTO pushCartItemDTO);

    @Transactional
    List<CartDTO> removeItemFromCart(Long userid, Long productid);

    @Transactional
    List<CartDTO> removeAllProducts(Long userid);

    List<CartItemDTO> getAllCartItems(Long userid);
}
