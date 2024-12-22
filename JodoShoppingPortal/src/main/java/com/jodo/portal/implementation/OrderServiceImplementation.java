package com.jodo.portal.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jodo.portal.dto.OrderDTO;
import com.jodo.portal.dto.OrderMapper;
import com.jodo.portal.exceptions.ResourceNotFoundException;
import com.jodo.portal.model.Bucket;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Order;
import com.jodo.portal.model.Product;
import com.jodo.portal.repository.EnduserRepository;
import com.jodo.portal.repository.OrderRepository;
import com.jodo.portal.repository.ProductRepository;

@Service
public class OrderServiceImplementation implements com.jodo.portal.service.OrderService {
	public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplementation.class);

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	EnduserRepository enduserRepository;

	@Autowired
	ProductRepository productRepository;

	OrderMapper orderMapper = new OrderMapper();
	@Override
	public OrderDTO placeOrder(Long userid, OrderDTO pushOrderDTO) {
	    Order pushOrder = orderMapper.toOrder(pushOrderDTO);
	    EndUser user = enduserRepository.findById(userid)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found with UserId " + userid));

	    if (user.getIsdelete() == 1) {
	        throw new ResourceNotFoundException("User with UserId " + userid + " is deleted.");
	    }

	    // Iterate through the bucket and merge duplicate products
	    List<Bucket> updatedBucket = new ArrayList<>();
	    
	    for (Bucket bucketItem : pushOrder.getBucket()) {
	        boolean productExists = false;

	        // Check if the product already exists in the updated bucket
	        for (Bucket existingItem : updatedBucket) {
	            if (existingItem.getProductid().equals(bucketItem.getProductid())) {
	                // Merge the quantities if the product is already in the bucket
	                int updatedQuantity = existingItem.getQuantity() + bucketItem.getQuantity();
	                existingItem.setQuantity(updatedQuantity);
	                productExists = true;
	                break;
	            }
	        }

	        // If the product is not in the updated bucket, add it
	        if (!productExists) {
	            updatedBucket.add(bucketItem);
	        }
	    }

	    // Set the updated bucket with merged quantities
	    pushOrder.setBucket(updatedBucket);

	    // Calculate the total order price based on the updated quantities
	    Double totalOrderPrice = updatedBucket.stream()
	            .map(this::calculateBucketItemPrice)
	            .reduce(0.0, Double::sum);

	    // Setting order details
	    pushOrder.setUser(user);
	    pushOrder.setTotalamount(totalOrderPrice);
	    pushOrder.setIsdelevered("NO");
	    pushOrder.setDate(sf.format(new Date()));
	    updatedBucket.forEach(bucketItem -> bucketItem.setOrder(pushOrder));
	    pushOrder.setStatus("ORDER_PLACED");
	    Order savedOrder = orderRepository.save(pushOrder);
	    return orderMapper.toOrderDTO(savedOrder);
	}


	private Double calculateBucketItemPrice(Bucket bucketItem) {
		Long productId = bucketItem.getProductid();
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with ProductId " + productId));

		if (product.getInstock() <= 0) {
			throw new ResourceNotFoundException("Product " + product.getName() + " is OUT OFF STOCK");
		}

		if (product.getInstock() < bucketItem.getQuantity()) {
			throw new ResourceNotFoundException(
					"For " + product.getName() + " you can order only " + product.getInstock() + " items");
		}

		// Update instock and quantity based on the ordered quantity
		int remainingStock = product.getInstock() - bucketItem.getQuantity();
		product.setInstock(remainingStock);
		product.setQuantity(remainingStock); // If 'quantity' is meant to store the remaining stock as well

		if (remainingStock == 0) {
			product.setProductstatus("OUT-OFF-STOCK");
		}

		// Save the product with updated stock and quantity
		productRepository.save(product);

		// Calculate the price for this bucket item
		double itemPrice = product.getPrice() * bucketItem.getQuantity();
		bucketItem.setPrice(itemPrice);
		bucketItem.setProduct(product);
		bucketItem.setProductorderstatus("ORDER_PLACED");

		return itemPrice;
	}

	@Override
	public OrderDTO getPlacedOrderDetailsByOrderId(Long orderid) {
		Optional<Order> retrivedOrder = orderRepository.findById(orderid);
		if (retrivedOrder.isEmpty()) {
			throw new ResourceNotFoundException("No OrderDetails found with OrderId " + orderid);
		}
		Order orderdetails = retrivedOrder.get();

		OrderDTO dto = orderMapper.toOrderDTO(orderdetails);
		orderdetails.getBucket().stream().forEach(bucket -> {
			long productid = bucket.getProduct().getId();
			dto.getBucket().forEach(dtoBucket -> {
				if (bucket.getId().equals(dtoBucket.getId())) {
					dtoBucket.setProductid(productid);
				}
			});
		});
		return dto;
	}

	@Override
	public List<Object> getOrdersByUserId(Long userid) {
		List<OrderDTO> orderDtoList = orderRepository
				.findByUser(enduserRepository.findById(userid)
						.orElseThrow(() -> new ResourceNotFoundException("User not found with UserId " + userid)))
				.stream().map(order -> getPlacedOrderDetailsByOrderId(order.getId())).collect(Collectors.toList());

		return new ArrayList<>(orderDtoList);
	}

	// Method to cancel an entire order
	@Override
	public OrderDTO cancelOrder(Long orderId) {
		// Retrieve the order by orderId
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with OrderId " + orderId));

		// Check if the order is already cancelled or delivered
		if (order.getStatus().equals("ORDER_CANCELLED")) {
			throw new ResourceNotFoundException("Order is already cancelled with OrderId " + orderId);
		}
		if (order.getStatus().equals("ORDER_DELIVERED")) {
			throw new IllegalStateException("Delivered orders cannot be cancelled.");
		}

		// Update the order status to "ORDER_CANCELLED"
		order.setStatus("ORDER_CANCELLED");

		// Restock products in the order
		for (Bucket bucket : order.getBucket()) {
			Product product = productRepository.findById(bucket.getProduct().getId()).get();			
			int updatedQuantity = product.getInstock() + bucket.getQuantity();
			product.setInstock(updatedQuantity);
			product.setQuantity(updatedQuantity);
			bucket.setProductorderstatus("ITEM_CANCELLED");
			productRepository.save(product);
		}
		order.setTotalamount(0);
		Order savedOrder = orderRepository.save(order);
		OrderDTO dto = orderMapper.toOrderDTO(savedOrder);

		savedOrder.getBucket().stream().forEach(bucket -> {
			long productid = bucket.getProduct().getId();
			dto.getBucket().forEach(dtoBucket -> {
				if (bucket.getId().equals(dtoBucket.getId())) {
					dtoBucket.setProductid(productid);
				}
			});
		});
		return dto;
	}

	// Method to cancel a specific item in an order
	@Override
	public OrderDTO cancelOrderItem(Long orderId, Long productId) {
		// Retrieve the order by orderId
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with OrderId " + orderId));

		// Find the bucket item by bucketItemId
		Bucket bucketItem = order.getBucket().stream().filter(item -> item.getProduct().getId().equals(productId))
				.findFirst().orElseThrow(
						() -> new ResourceNotFoundException("Item not found in the order with productId " + productId));

		// Check if the item is already cancelled
		if (bucketItem.getProductorderstatus().equals("ITEM_CANCELLED")) {
			throw new ResourceNotFoundException("Item is already cancelled with productId " + productId);
		}

		// Update the bucket item status to "ITEM_CANCELLED"
		bucketItem.setProductorderstatus("ITEM_CANCELLED");

		Product product = productRepository.findById(bucketItem.getProduct().getId()).get();			
		int updatedQuantity = product.getInstock() + bucketItem.getQuantity();
		product.setInstock(updatedQuantity);
		product.setQuantity(updatedQuantity);
		bucketItem.setProductorderstatus("ITEM_CANCELLED");
		productRepository.save(product);
		
		order.setTotalamount(order.getTotalamount() - (bucketItem.getQuantity()*product.getPrice()));
		Order savedOrder = orderRepository.save(order);
		OrderDTO dto = orderMapper.toOrderDTO(savedOrder);

		savedOrder.getBucket().stream().forEach(bucket -> {
			long productid = bucket.getProduct().getId();
			dto.getBucket().forEach(dtoBucket -> {
				if (bucket.getId().equals(dtoBucket.getId())) {
					dtoBucket.setProductid(productid);
				}
			});
		});
		return dto;
	}
}
