package com.agroconnect.controller;

import com.agroconnect.dto.AdminDashboardDto;
import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.UserDto;
import com.agroconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<String> toggleUserBlock(@PathVariable Long id) {
        adminService.toggleUserBlock(id);
        return ResponseEntity.ok("User block status updated successfully.");
    }

    @GetMapping("/farmers")
    public ResponseEntity<List<FarmerDto>> getAllFarmers() {
        return ResponseEntity.ok(adminService.getAllFarmers());
    }

    @PutMapping("/farmers/{id}/verify")
    public ResponseEntity<String> verifyFarmer(@PathVariable Long id) {
        adminService.verifyFarmer(id);
        return ResponseEntity.ok("Farmer verified successfully.");
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/products")
    public ResponseEntity<List<com.agroconnect.dto.ProductDto>> getAllProducts() {
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok("Inappropriate product removed successfully.");
    }
}
