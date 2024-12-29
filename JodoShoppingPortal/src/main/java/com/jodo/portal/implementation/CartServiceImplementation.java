package com.jodo.portal.implementation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jodo.portal.dto.CartDTO;
import com.jodo.portal.dto.CartItemDTO;
import com.jodo.portal.dto.CartMapper;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.Cart;
import com.jodo.portal.model.CartItem;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Product;
import com.jodo.portal.redis.RedisUtil;
import com.jodo.portal.repository.CartRepository;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImplementation implements com.jodo.portal.service.CartService {

	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(CartServiceImplementation.class);

	@Autowired
	private CartMapper cartMapper;

	@Autowired
	public RedisUtil redis;

	@Autowired
	CartRepository cartRepository;

	@Autowired
	EnduserRepository enduserRepository;

	@Autowired
	ProductRepository productRepository;

	@Override
	public CartDTO addProductToCart(Long userid, CartItemDTO pushCartItemDTO) {
		// Retrieve the user
		EndUser retrivedUser = enduserRepository.findById(userid)
				.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + userid));

		// Check if the user is active and not deleted
		if (retrivedUser.getIsactive() == 0 || retrivedUser.getIsdelete() == 1) {
			throw new ResourceNotFoundException("No active user found with UserId " + userid);
		}

		// Retrieve the user's cart
		Cart userCart = retrivedUser.getCart();
		if (userCart == null) {
			// Initialize a new cart if the user doesn't have one
			userCart = new Cart();
			userCart.setTotalamount(0);
			userCart.setCartitem(List.of());
			retrivedUser.setCart(userCart);
		}

		// Retrieve the product
		Product fetchedProduct = productRepository.findById(pushCartItemDTO.getProductid())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Product with ProductId " + pushCartItemDTO.getProductid() + " not found"));

		// Ensure product is not deleted
		if (fetchedProduct.getIsdelete() == 1) {
			throw new ResourceNotFoundException(
					"Product with ProductId " + pushCartItemDTO.getProductid() + " is deleted");
		}

		// Find if the product is already in the cart
		Optional<CartItem> existingCartItem = userCart.getCartitem().stream()
				.filter(item -> item.getProduct().getId().equals(fetchedProduct.getId())).findFirst();

		CartItem cartItem;
		if (existingCartItem.isPresent()) {
			// Update the existing cart item
			cartItem = existingCartItem.get();
			int newQuantity = cartItem.getQuantity() + pushCartItemDTO.getQuantity();
			cartItem.setQuantity(newQuantity);
			cartItem.setPrice(cartItem.getPrice() + (fetchedProduct.getPrice() * pushCartItemDTO.getQuantity()));
		} else {
			// Create a new cart item if not already in the cart
			cartItem = new CartItem();
			cartItem.setProduct(fetchedProduct);
			cartItem.setQuantity(pushCartItemDTO.getQuantity());
			cartItem.setPrice(fetchedProduct.getPrice() * pushCartItemDTO.getQuantity());
			cartItem.setCart(userCart);
			userCart.getCartitem().add(cartItem); // Add to the cart's list of items
		}

		double newTotalAmount = userCart.getCartitem().stream().mapToDouble(CartItem::getPrice).sum();
		userCart.setTotalamount(newTotalAmount);

		Cart savedCart = cartRepository.save(userCart);
		logger.info("Product added to cart for user {}", userid);

		CartDTO dto = cartMapper.convertToCartDTO(savedCart);
		return dto;
	}

	@Override
	@Transactional
	public CartDTO removeItemFromCart(Long userid, Long productid) {
		// Retrieve the user
		EndUser retrivedUser = enduserRepository.findById(userid)
				.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + userid));

		if (retrivedUser.getIsactive() == 0 || retrivedUser.getIsdelete() == 1) {
			throw new ResourceNotFoundException("No active user found with UserId " + userid);
		}

		Cart userCart = retrivedUser.getCart();
		if (userCart == null) {
			// Initialize a new cart if the user doesn't have one
			userCart = new Cart();
			userCart.setTotalamount(0);
			userCart.setCartitem(List.of());
			retrivedUser.setCart(userCart);
		}

		Product fetchedProduct = productRepository.findById(productid)
				.orElseThrow(() -> new ResourceNotFoundException("Product with ProductId " + productid + " not found"));

		if (fetchedProduct.getIsdelete() == 1) {
			throw new ResourceNotFoundException("Product with ProductId " + productid + " is deleted");
		}
		Optional<CartItem> existingCartItem = userCart.getCartitem().stream()
				.filter(item -> item.getProduct().getId().equals(fetchedProduct.getId())).findFirst();

		if (existingCartItem.isEmpty()) {
			throw new ResourceNotFoundException("No such product exists in the cart");
		}

		CartItem cartItemToRemove = existingCartItem.get();
		cartItemToRemove.setCart(null);
		userCart.getCartitem().remove(cartItemToRemove);

		double newTotalAmount = userCart.getCartitem().stream().mapToDouble(CartItem::getPrice).sum();
		userCart.setTotalamount(newTotalAmount);
		Cart savedCart = cartRepository.save(userCart);
		return cartMapper.convertToCartDTO(savedCart);
	}

	@Override
	@Transactional
	public CartDTO removeAllProducts(Long userid) {
		EndUser retrivedUser = enduserRepository.findById(userid)
				.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + userid));

		if (retrivedUser.getIsactive() == 0 || retrivedUser.getIsdelete() == 1) {
			throw new ResourceNotFoundException("No active user found with UserId " + userid);
		}

		Cart userCart = retrivedUser.getCart();
		if (userCart == null) {
			// Initialize a new cart if the user doesn't have one
			userCart = new Cart();
			userCart.setTotalamount(0);
			userCart.setCartitem(List.of());
			retrivedUser.setCart(userCart);
		}

		List<CartItem> cartItems = userCart.getCartitem();
		if (cartItems.isEmpty()) {
			throw new ResourceNotFoundException("No products found in the cart to remove");
		}
		userCart.getCartitem().forEach(item -> item.setCart(null));
		userCart.setUser(null);
		userCart.getCartitem().clear();
		userCart.setTotalamount(0.0);
		Cart savedCart = cartRepository.save(userCart);
		return cartMapper.convertToCartDTO(savedCart);
	}

	@Override
	public List<Object> getAllCartItems(Long userid) {
		EndUser retrievedUser = enduserRepository.findById(userid)
				.orElseThrow(() -> new ResourceNotFoundException("No user found with UserId " + userid));

		if (retrievedUser.getIsactive() == 0 || retrievedUser.getIsdelete() == 1) {
			throw new ResourceNotFoundException("No active user found with UserId " + userid);
		}
		Cart userCart = retrievedUser.getCart();
		if (userCart == null) {
			userCart = new Cart();
			userCart.setTotalamount(0);
			userCart.setCartitem(List.of());
			retrievedUser.setCart(userCart);
		}
		List<Object> data = userCart.getCartitem().stream().map(item -> cartMapper.convertToCartItemDTO(item))
				.collect(Collectors.toList());
		return data;
	}
}
