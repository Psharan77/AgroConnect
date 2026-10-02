package com.agroconnect.service;

import com.agroconnect.dto.FarmerDto;
import com.agroconnect.dto.ProductDto;

import java.util.List;

public interface FarmerService {
    FarmerDto createFarmer(FarmerDto farmerDto);
    FarmerDto getFarmerById(Long id);
    List<FarmerDto> getAllFarmers();
    FarmerDto updateFarmer(Long id, FarmerDto farmerDto);
    void deleteFarmer(Long id);
    List<ProductDto> getProductsByFarmerId(Long farmerId);
}
