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

import com.jodo.portal.dto.OrderDTO;
import com.jodo.portal.model.ResponseApi;
import com.jodo.portal.implementation.OrderServiceImplementation;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private OrderServiceImplementation orderService;

    @PostMapping("/order/{userid}")
    public ResponseEntity<ResponseApi> pushOrder(@PathVariable("userid") Long userid, @RequestBody OrderDTO order) {
        List<OrderDTO> savedOrder = orderService.placeOrder(userid, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.createResponse(1,
                "Order placed successfully", savedOrder));
    }

    @GetMapping("/order/details/{orderid}")
    public ResponseEntity<ResponseApi> getOrderByOrderId(@PathVariable("orderid") Long orderid) {
        List<OrderDTO> retrievedOrder = orderService.getPlacedOrderDetailsByOrderId(orderid);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseApi.createResponse(1,
                "Order retrieved successfully", retrievedOrder));
    }

    @GetMapping("/order/user/{userid}")
    public ResponseEntity<ResponseApi> getOrderByUserId(@PathVariable("userid") Long userid) {
        List<List<OrderDTO>> orderList = orderService.getOrdersByUserId(userid);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Orders retrieved successfully", orderList));
    }
    
    @DeleteMapping("/order/cancel/{orderId}")
    public ResponseEntity<ResponseApi> cancelOrder(@PathVariable("orderId") Long orderId) {
        List<OrderDTO> canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Order cancelled successfully.", canceledOrder));
    }
    
    @DeleteMapping("/order/cancel_item/{orderId}/{itemId}")
    public ResponseEntity<ResponseApi> cancelOrderItem(@PathVariable("orderId") Long orderId, 
                                                       @PathVariable("itemId") Long itemId) {
        List<OrderDTO> updatedOrder = orderService.cancelOrderItem(orderId, itemId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Oder Item cancelled successfully.", updatedOrder));
    }
}
