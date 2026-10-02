package com.agroconnect.repository;

import com.agroconnect.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByOrder_Customer_IdAndProduct_Id(Long customerId, Long productId);
    boolean existsByOrder_Customer_IdAndProduct_IdAndOrder_Status(Long customerId, Long productId, String status);

    @Query("SELECT COUNT(DISTINCT i.order.id) FROM OrderItem i WHERE i.product.farmer.id = :farmerId")
    Long countDistinctOrdersByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT COUNT(DISTINCT i.order.id) FROM OrderItem i WHERE i.product.farmer.id = :farmerId AND i.order.status = :status")
    Long countDistinctOrdersByFarmerIdAndStatus(@Param("farmerId") Long farmerId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.quantity * i.unitPrice), 0) FROM OrderItem i WHERE i.product.farmer.id = :farmerId AND i.order.status = 'DELIVERED'")
    BigDecimal calculateTotalSalesByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(i.quantity * i.unitPrice), 0) FROM OrderItem i WHERE i.product.farmer.id = :farmerId AND i.order.status = 'DELIVERED' AND i.order.orderDate >= :startDate AND i.order.orderDate <= :endDate")
    BigDecimal calculateSalesByFarmerIdAndDateRange(
            @Param("farmerId") Long farmerId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
