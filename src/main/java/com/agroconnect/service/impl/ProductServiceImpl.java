package com.agroconnect.service.impl;

import com.agroconnect.dto.ProductDto;
import com.agroconnect.entity.Farmer;
import com.agroconnect.entity.Product;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.ProductRepository;
import com.agroconnect.service.ProductService;
import lombok.RequiredArgsConstructor;
import com.agroconnect.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = mapToEntity(productDto);
        
        // SECURITY: Force the farmer ID to the currently authenticated farmer
        Long currentFarmerId = securityUtils.getCurrentFarmerId();
        Farmer farmer = new Farmer();
        farmer.setId(currentFarmerId);
        product.setFarmer(farmer);
        
        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    @Override
    public List<ProductDto> getAllProducts(Long categoryId, String location, Boolean organic) {
        List<Product> products;
        
        // Handling dynamic filtering
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else if (location != null) {
            products = productRepository.findByLocationContainingIgnoreCase(location);
        } else if (organic != null) {
            products = productRepository.findByOrganic(organic);
        } else {
            products = productRepository.findAll();
        }
        
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Long currentFarmerId = securityUtils.getCurrentFarmerId();
        if (product.getFarmer() == null || !product.getFarmer().getId().equals(currentFarmerId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this product.");
        }

        // Update fields
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setQuantity(productDto.getQuantity());
        product.setUnit(productDto.getUnit());
        product.setImageUrl(productDto.getImageUrl());
        product.setOrganic(productDto.getOrganic());
        if (productDto.getCategoryId() != null) {
            com.agroconnect.entity.Category category = new com.agroconnect.entity.Category();
            category.setId(productDto.getCategoryId());
            product.setCategory(category);
        }
        product.setLocation(productDto.getLocation());

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        Long currentFarmerId = securityUtils.getCurrentFarmerId();
        if (product.getFarmer() == null || !product.getFarmer().getId().equals(currentFarmerId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to delete this product.");
        }
        
        productRepository.delete(product);
    }

    @Override
    public List<ProductDto> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsByFarmerId(Long farmerId) {
        List<Product> products = productRepository.findByFarmerId(farmerId);
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Convert Entity to DTO
    private ProductDto mapToDto(Product product) {
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
        dto.setAverageRating(product.getAverageRating() != null ? Math.round(product.getAverageRating() * 10.0) / 10.0 : 0.0);
        dto.setReviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0L);
        return dto;
    }

    // Convert DTO to Entity
    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        if (dto.getFarmerId() != null) {
            Farmer farmer = new Farmer();
            farmer.setId(dto.getFarmerId());
            product.setFarmer(farmer);
        }
        if (dto.getCategoryId() != null) {
            com.agroconnect.entity.Category category = new com.agroconnect.entity.Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setUnit(dto.getUnit());
        product.setImageUrl(dto.getImageUrl());
        product.setOrganic(dto.getOrganic());
        product.setLocation(dto.getLocation());
        return product;
    }
}
