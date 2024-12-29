package com.jodo.portal.dto;
import org.springframework.stereotype.Component;

import com.jodo.portal.model.Cart;
import com.jodo.portal.model.CartItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO convertToCartDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setTotalamount(cart.getTotalamount());
        dto.setCartItems(cart.getCartitem() != null ? 
            cart.getCartitem().stream().map(this::convertToCartItemDTO).collect(Collectors.toList()) : 
            List.of());
        return dto;
    }

    public Cart convertToCart(CartDTO cartDTO) {
        Cart cart = new Cart();
        cart.setId(cartDTO.getId());
        cart.setTotalamount(cartDTO.getTotalamount());
        cart.setCartitem(cartDTO.getCartItems() != null ? 
            cartDTO.getCartItems().stream().map(this::convertToCartItem).collect(Collectors.toList()) : 
            List.of());
        return cart;
    }

    public CartItemDTO convertToCartItemDTO(CartItem cartItem) {
    	ProductMapper productMapper =new  ProductMapper();
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setPrice(cartItem.getPrice());
        dto.setQuantity(cartItem.getQuantity());
//        dto.setProductid(cartItem.getProduct() != null ? cartItem.getProduct().getId().intValue() : null); // Assuming product ID is long
        dto.setProduct(productMapper.convertToProductDTO(cartItem.getProduct()));
        dto.setProductid(dto.getProduct().getId());
        return dto;
    }

    public CartItem convertToCartItem(CartItemDTO cartItemDTO) {
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemDTO.getId());
        cartItem.setPrice(cartItemDTO.getPrice());
        cartItem.setQuantity(cartItemDTO.getQuantity());
        // Add conversion for Product if needed
        return cartItem;
    }
}
