package com.jodo.portal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.jodo.portal.model.Bucket;
import com.jodo.portal.model.Order;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public OrderDTO toOrderDTO(Order order) {
        if (order == null) {
            return null;
        }
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setDate(order.getDate());
        orderDTO.setTotalamount(order.getTotalamount());
        orderDTO.setPaymentmode(order.getPaymentmode());
        orderDTO.setIspaymentdone(order.getIspaymentdone());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setIsdelevered(order.getIsdelevered());
        
        List<BucketDTO> bucketDTOs = order.getBucket().stream()
                .map(this::toBucketDTO)
                .collect(Collectors.toList());
        orderDTO.setBucket(bucketDTOs);

        return orderDTO;
    }

    public Order toOrder(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setDate(orderDTO.getDate());
        order.setTotalamount(orderDTO.getTotalamount());
        order.setPaymentmode(orderDTO.getPaymentmode());
        order.setIspaymentdone(orderDTO.getIspaymentdone());
        order.setStatus(orderDTO.getStatus());
        order.setIsdelevered(orderDTO.getIsdelevered());

        List<Bucket> buckets = orderDTO.getBucket().stream()
                .map(this::toBucket)
                .collect(Collectors.toList());
        order.setBucket(buckets);

        return order;
    }

    public BucketDTO toBucketDTO(Bucket bucket) {
    	ProductMapper productMapper = new ProductMapper();
        if (bucket == null) {
            return null;
        }
        BucketDTO bucketDTO = new BucketDTO();
        bucketDTO.setId(bucket.getId());
        bucketDTO.setPrice(bucket.getPrice());
        bucketDTO.setProductid(bucket.getProductid());
        bucketDTO.setProduct( productMapper.convertToProductDTO(bucket.getProduct()));
        bucketDTO.setQuantity(bucket.getQuantity());
        bucketDTO.setProductorderstatus(bucket.getProductorderstatus());

        return bucketDTO;
    }

    public Bucket toBucket(BucketDTO bucketDTO) {
    	ProductMapper productMapper = new ProductMapper();
        if (bucketDTO == null) {
            return null;
        }
        Bucket bucket = new Bucket();
        bucket.setId(bucketDTO.getId());
        bucket.setPrice(bucketDTO.getPrice());
        bucket.setProductid(bucketDTO.getProductid());
//        bucket.setProduct(productMapper.convertToProduct(bucketDTO.getProduct()));
        bucket.setQuantity(bucketDTO.getQuantity());
        bucket.setProductorderstatus(bucketDTO.getProductorderstatus());
        return bucket;
    }
}
