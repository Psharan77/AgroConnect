package com.agroconnect.service.impl;

import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.OrderItemDto;
import com.agroconnect.dto.OrderRequestDto;
import com.agroconnect.entity.*;
import com.agroconnect.exception.BadRequestException;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.*;
import com.agroconnect.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDto createOrder(Long customerId, OrderRequestDto request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));

        // Validate address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setAddress(address);
        order.setStatus("PLACED");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Stock Check
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Product '" + product.getName() + "' does not have enough stock");
            }

            // Deduct Stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice()); // Snapshot the price

            orderItems.add(orderItem);
            
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToDto(savedOrder);
    }

    @Override
    public List<OrderDto> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDto(order);
    }

    @Override
    public List<OrderDto> getFarmerOrders(Long farmerId) {
        return orderRepository.findOrdersByFarmerId(farmerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto updateOrderStatus(Long farmerId, Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isOwner = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getFarmer() != null && item.getProduct().getFarmer().getId().equals(farmerId));
        if (!isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this order.");
        }

        if (order.getStatus().equals("CANCELLED")) {
            throw new BadRequestException("Cannot update a cancelled order");
        }

        order.setStatus(status.toUpperCase());
        return mapToDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Not authorized to cancel this order");
        }

        if (!order.getStatus().equals("PLACED") && !order.getStatus().equals("CONFIRMED")) {
            throw new BadRequestException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");

        // Restore stock for all products
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        return mapToDto(orderRepository.save(order));
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setAddressId(order.getAddress().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
