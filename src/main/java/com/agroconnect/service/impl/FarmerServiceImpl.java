package com.agroconnect.service.impl;

import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.ProductDto;
import com.agroconnect.entity.Farmer;
import com.agroconnect.entity.Product;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.FarmerRepository;
import com.agroconnect.service.FarmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.agroconnect.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;

    @Override
    public FarmerDto createFarmer(FarmerDto farmerDto) {
        Farmer farmer = mapToEntity(farmerDto);
        if (farmer.getVerified() == null) {
            farmer.setVerified(false);
        }
        Farmer savedFarmer = farmerRepository.save(farmer);
        return mapToDto(savedFarmer);
    }

    @Override
    public FarmerDto getFarmerById(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + id));
        return mapToDto(farmer);
    }

    @Override
    public List<FarmerDto> getAllFarmers() {
        return farmerRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public FarmerDto updateFarmer(Long id, FarmerDto farmerDto) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + id));

        farmer.setFarmName(farmerDto.getFarmName());
        farmer.setLocation(farmerDto.getLocation());
        farmer.setDescription(farmerDto.getDescription());
        farmer.setPhone(farmerDto.getPhone());
        
        Farmer updatedFarmer = farmerRepository.save(farmer);
        return mapToDto(updatedFarmer);
    }

    @Override
    public void deleteFarmer(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + id));
        farmerRepository.delete(farmer);
    }

    @Override
    public List<ProductDto> getProductsByFarmerId(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));
                
        // Fetching products through the JPA relationship
        return farmer.getProducts().stream().map(this::mapProductToDto).collect(Collectors.toList());
    }

    // Helper mappings
    private FarmerDto mapToDto(Farmer farmer) {
        FarmerDto dto = new FarmerDto();
        dto.setId(farmer.getId());
        dto.setUserId(farmer.getUser() != null ? farmer.getUser().getId() : null);
        dto.setFarmName(farmer.getFarmName());
        dto.setLocation(farmer.getLocation());
        dto.setDescription(farmer.getDescription());
        dto.setPhone(farmer.getPhone());
        dto.setVerified(farmer.getVerified());
        dto.setCreatedAt(farmer.getCreatedAt());
        return dto;
    }

    private Farmer mapToEntity(FarmerDto dto) {
        Farmer farmer = new Farmer();
        farmer.setUser(userRepository.findById(dto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        farmer.setFarmName(dto.getFarmName());
        farmer.setLocation(dto.getLocation());
        farmer.setDescription(dto.getDescription());
        farmer.setPhone(dto.getPhone());
        farmer.setVerified(dto.getVerified());
        return farmer;
    }
    
    // Map Product Entity to DTO
    private ProductDto mapProductToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setFarmerId(product.getFarmer() != null ? product.getFarmer().getId() : null);
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setUnit(product.getUnit());
        dto.setImageUrl(product.getImageUrl());
        dto.setOrganic(product.getOrganic());
        dto.setLocation(product.getLocation());
        return dto;
    }
}
