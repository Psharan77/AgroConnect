package com.agroconnect.service;

import com.agroconnect.dto.AdminDashboardDto;
import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.UserDto;

import java.util.List;

public interface AdminService {
    AdminDashboardDto getDashboardStats();
    List<UserDto> getAllUsers();
    List<FarmerDto> getAllFarmers();
    void verifyFarmer(Long id);
    List<OrderDto> getAllOrders();
    List<com.agroconnect.dto.ProductDto> getAllProducts();
    void deleteProduct(Long id);
    void toggleUserBlock(Long id);
}
