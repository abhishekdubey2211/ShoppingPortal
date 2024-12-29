package com.jodo.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jodo.portal.dto.CartDTO;
import com.jodo.portal.dto.CartItemDTO;
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.implementation.CartServiceImplementation;

@RestController
@RequestMapping("/api/v1")
public class CartController {

	@Autowired
	private CartServiceImplementation cartServiceImplementation;

	@PostMapping("/cart/{userid}")
	public ResponseEntity<ResponseApi> addProductToCart(@RequestBody CartItemDTO cartItem,
			@PathVariable("userid") Long userid) throws Exception {
		List<CartDTO> savedCart = cartServiceImplementation.addProductToCart(userid, cartItem);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseApi.createResponse(1, "Product added to your cart successfully.", savedCart));
	}

	@DeleteMapping("/cart/{userid}/{productid}")
	public ResponseEntity<ResponseApi> removeProductFromCart(@PathVariable("userid") Long userid,
			@PathVariable("productid") Long productid) throws Exception {
		List<CartDTO> savedCart = cartServiceImplementation.removeItemFromCart(userid, productid);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "Product removed from your cart successfully.", savedCart));
	}

	@DeleteMapping("/cart/flushproduct/{userid}")
	public ResponseEntity<ResponseApi> removeAllProductsFromCart(@PathVariable("userid") Long userid) throws Exception {
		List<CartDTO> savedCart = cartServiceImplementation.removeAllProducts(userid);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All products removed from your cart successfully.", savedCart));
	}

	@GetMapping("/cart/get_products/{userid}")
	public ResponseEntity<ResponseApi> getUserCartDetails(@PathVariable("userid") Long userid) throws Exception {
		List<CartItemDTO> cartList = cartServiceImplementation.getAllCartItems(userid);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseApi.createResponse(1, "All products fetched from your cart successfully.", cartList));
	}
}
