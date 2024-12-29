package com.jodo.portal.service;

import com.jodo.portal.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(Long userid, OrderDTO pushOrderDTO);

    OrderDTO getPlacedOrderDetailsByOrderId(Long orderid);

    List<Object> getOrdersByUserId(Long userid);

    // Method to cancel an entire order
    OrderDTO cancelOrder(Long orderId);

    // Method to cancel a specific item in an order
    OrderDTO cancelOrderItem(Long orderId, Long productId);
}
