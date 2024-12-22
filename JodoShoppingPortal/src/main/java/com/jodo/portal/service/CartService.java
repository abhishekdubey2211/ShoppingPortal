package com.jodo.portal.service;

import com.jodo.portal.dto.CartDTO;
import com.jodo.portal.dto.CartItemDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long userid, CartItemDTO pushCartItemDTO);

    @Transactional
    CartDTO removeItemFromCart(Long userid, Long productid);

    @Transactional
    CartDTO removeAllProducts(Long userid);

    List<Object> getAllCartItems(Long userid);
}
