package com.agroconnect.service.impl;

import com.agroconnect.dto.AdminDashboardDto;
import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.UserDto;
import com.agroconnect.entity.Farmer;
import com.agroconnect.entity.Order;
import com.agroconnect.entity.Role;
import com.agroconnect.entity.User;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.FarmerRepository;
import com.agroconnect.repository.OrderRepository;
import com.agroconnect.repository.ProductRepository;
import com.agroconnect.repository.UserRepository;
import com.agroconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.agroconnect.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalCustomers(userRepository.countByRole(Role.CUSTOMER));
        dto.setTotalFarmers(userRepository.countByRole(Role.FARMER));
        dto.setTotalProducts(productRepository.count());
        dto.setActiveProducts(productRepository.countByQuantityLessThanEqualAndQuantityGreaterThan(Integer.MAX_VALUE, 0)); // simple way for active
        dto.setTotalOrders(orderRepository.count());
        dto.setTotalReviews(reviewRepository.count());
        dto.setTotalSales(orderRepository.calculateTotalRevenue());
        
        dto.setPlacedOrders(orderRepository.countByStatus("PLACED"));
        dto.setConfirmedOrders(orderRepository.countByStatus("CONFIRMED"));
        dto.setPackedOrders(orderRepository.countByStatus("PACKED"));
        dto.setShippedOrders(orderRepository.countByStatus("SHIPPED"));
        dto.setDeliveredOrders(orderRepository.countByStatus("DELIVERED"));
        dto.setCancelledOrders(orderRepository.countByStatus("CANCELLED"));
        
        dto.setLowStockProducts(productRepository.countByQuantityLessThanEqualAndQuantityGreaterThan(5, 0));
        dto.setOutOfStockProducts(productRepository.countByQuantity(0));
        return dto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(u -> {
            UserDto dto = new UserDto();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setFullName(u.getFullName());
            dto.setRole(u.getRole().name());
            dto.setBlocked(u.getBlocked());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FarmerDto> getAllFarmers() {
        return farmerRepository.findAll().stream().map(f -> {
            FarmerDto dto = new FarmerDto();
            dto.setId(f.getId());
            dto.setUserId(f.getUser() != null ? f.getUser().getId() : null);
            dto.setFarmName(f.getFarmName());
            dto.setLocation(f.getLocation());
            dto.setDescription(f.getDescription());
            dto.setPhone(f.getPhone());
            dto.setVerified(f.getVerified());
            dto.setCreatedAt(f.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void verifyFarmer(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
        farmer.setVerified(true);
        farmerRepository.save(farmer);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream().map(o -> {
            OrderDto dto = new OrderDto();
            dto.setId(o.getId());
            dto.setCustomerId(o.getCustomer().getId());
            dto.setAddressId(o.getAddress().getId());
            dto.setTotalAmount(o.getTotalAmount());
            dto.setStatus(o.getStatus());
            dto.setOrderDate(o.getOrderDate());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Override
    public void toggleUserBlock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBlocked(user.getBlocked() == null || !user.getBlocked());
        userRepository.save(user);
    }
    
    @Override
    public List<com.agroconnect.dto.ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(p -> {
            com.agroconnect.dto.ProductDto dto = new com.agroconnect.dto.ProductDto();
            dto.setId(p.getId());
            dto.setFarmerId(p.getFarmer() != null ? p.getFarmer().getId() : null);
            dto.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
            dto.setName(p.getName());
            dto.setPrice(p.getPrice());
            dto.setQuantity(p.getQuantity());
            dto.setUnit(p.getUnit());
            return dto;
        }).collect(Collectors.toList());
    }
}
