package com.agroconnect.service;

import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.OrderRequestDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(Long customerId, OrderRequestDto request);
    List<OrderDto> getCustomerOrders(Long customerId);
    OrderDto getOrderById(Long orderId);
    List<OrderDto> getFarmerOrders(Long farmerId);
    OrderDto updateOrderStatus(Long farmerId, Long orderId, String status);
    OrderDto cancelOrder(Long customerId, Long orderId);
}
