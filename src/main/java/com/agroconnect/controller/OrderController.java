package com.agroconnect.controller;

import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.OrderRequestDto;
import com.agroconnect.dto.OrderStatusUpdateRequestDto;
import com.agroconnect.security.SecurityUtils;
import com.agroconnect.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        return new ResponseEntity<>(orderService.createOrder(securityUtils.getCurrentCustomerId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        return ResponseEntity.ok(orderService.getCustomerOrders(securityUtils.getCurrentCustomerId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        if (!order.getCustomerId().equals(securityUtils.getCurrentCustomerId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to view this order.");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/farmer")
    public ResponseEntity<List<OrderDto>> getFarmerOrders() {
        return ResponseEntity.ok(orderService.getFarmerOrders(securityUtils.getCurrentFarmerId()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequestDto request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(securityUtils.getCurrentFarmerId(), id, request.getStatus()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(securityUtils.getCurrentCustomerId(), id));
    }
}
