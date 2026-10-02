package com.agroconnect.controller;

import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.ProductDto;
import com.agroconnect.service.FarmerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;
    private final com.agroconnect.security.SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<FarmerDto> createFarmer(@Valid @RequestBody FarmerDto farmerDto) {
        farmerDto.setUserId(securityUtils.getCurrentUser().getId());
        FarmerDto createdFarmer = farmerService.createFarmer(farmerDto);
        return new ResponseEntity<>(createdFarmer, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FarmerDto>> getAllFarmers() {
        return ResponseEntity.ok(farmerService.getAllFarmers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmerDto> getFarmerById(@PathVariable Long id) {
        return ResponseEntity.ok(farmerService.getFarmerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FarmerDto> updateFarmer(@PathVariable Long id, @Valid @RequestBody FarmerDto farmerDto) {
        return ResponseEntity.ok(farmerService.updateFarmer(id, farmerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.ok("Farmer profile deleted successfully.");
    }
    
    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductDto>> getProductsByFarmerId(@PathVariable Long id) {
        return ResponseEntity.ok(farmerService.getProductsByFarmerId(id));
    }
}
