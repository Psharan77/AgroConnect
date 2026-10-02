package com.agroconnect.service;

import com.agroconnect.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto);
    List<ProductDto> getAllProducts(Long categoryId, String location, Boolean organic);
    ProductDto getProductById(Long id);
    ProductDto updateProduct(Long id, ProductDto productDto);
    void deleteProduct(Long id);
    List<ProductDto> searchProductsByName(String name);
    List<ProductDto> getProductsByFarmerId(Long farmerId);
}
