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
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderServiceImplementation orderService;

    @PostMapping("/v4/order/{userid}")
    public ResponseEntity<ResponseApi> pushOrder(@PathVariable("userid") Long userid, @RequestBody OrderDTO order) {
        List<Object> orderList = new ArrayList<>();
        OrderDTO savedOrder = orderService.placeOrder(userid, order);
        orderList.add(savedOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.createResponse(1,
                "Order placed successfully with Order ID: " + savedOrder.getId(), orderList));
    }

    @GetMapping("/v3/order/details/{orderid}")
    public ResponseEntity<ResponseApi> getOrderByOrderId(@PathVariable("orderid") Long orderid) {
        List<Object> orderList = new ArrayList<>();
        OrderDTO retrievedOrder = orderService.getPlacedOrderDetailsByOrderId(orderid);
        orderList.add(retrievedOrder);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseApi.createResponse(1,
                "Order retrieved successfully with Order ID: " + retrievedOrder.getId(), orderList));
    }

    @GetMapping("/v2/order/user/{userid}")
    public ResponseEntity<ResponseApi> getOrderByUserId(@PathVariable("userid") Long userid) {
        List<Object> orderList = orderService.getOrdersByUserId(userid);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Orders retrieved successfully for User ID: " + userid, orderList));
    }
    
    @DeleteMapping("/v4/order/cancel/{orderId}")
    public ResponseEntity<ResponseApi> cancelOrder(@PathVariable("orderId") Long orderId) {
        List<Object> responseList = new ArrayList<>();
        OrderDTO canceledOrder = orderService.cancelOrder(orderId);
        responseList.add(canceledOrder);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Order canceled successfully.", responseList));
    }
    
    @DeleteMapping("/v4/order/cancel_item/{orderId}/{itemId}")
    public ResponseEntity<ResponseApi> cancelOrderItem(@PathVariable("orderId") Long orderId, 
                                                       @PathVariable("itemId") Long itemId) {
        List<Object> responseList = new ArrayList<>();
        OrderDTO updatedOrder = orderService.cancelOrderItem(orderId, itemId);
        responseList.add(updatedOrder);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseApi.createResponse(1, "Item canceled from order successfully.", responseList));
    }
}
