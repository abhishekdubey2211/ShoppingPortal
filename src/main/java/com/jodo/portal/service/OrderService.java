package com.jodo.portal.service;

import com.jodo.portal.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> placeOrder(Long userid, OrderDTO pushOrderDTO);

    List<OrderDTO> getPlacedOrderDetailsByOrderId(Long orderid);

    List<List<OrderDTO>> getOrdersByUserId(Long userid);

    // Method to cancel an entire order
    List<OrderDTO> cancelOrder(Long orderId);

    // Method to cancel a specific item in an order
    List<OrderDTO> cancelOrderItem(Long orderId, Long productId);
}
