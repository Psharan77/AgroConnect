package com.agroconnect.repository;

import com.agroconnect.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    java.math.BigDecimal calculateTotalRevenue();
    
    Long countByStatus(String status);

    List<Order> findByCustomerId(Long customerId);
    
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.farmer.id = :farmerId")
    List<Order> findOrdersByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.farmer.id = :farmerId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByFarmerId(@Param("farmerId") Long farmerId, Pageable pageable);
}
