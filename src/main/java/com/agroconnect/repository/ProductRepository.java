package com.agroconnect.repository;

import com.agroconnect.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByLocationContainingIgnoreCase(String location);
    List<Product> findByOrganic(Boolean organic);
    List<Product> findByFarmerId(Long farmerId);
    
    // Aggregation queries for Dashboard
    Long countByFarmerId(Long farmerId);
    
    Long countByFarmerIdAndQuantityLessThanEqual(Long farmerId, Integer quantity);
    List<Product> findByFarmerIdAndQuantityLessThanEqual(Long farmerId, Integer quantity);
    
    Long countByQuantityLessThanEqualAndQuantityGreaterThan(Integer max, Integer min);
    Long countByQuantity(Integer quantity);

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Product p WHERE p.farmer.id = :farmerId")
    Integer sumAvailableStockByFarmerId(@Param("farmerId") Long farmerId);
}
