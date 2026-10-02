package com.agroconnect.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminDashboardDto {
    private Long totalCustomers;
    private Long totalFarmers;
    private Long totalProducts;
    private Long activeProducts;
    private Long totalOrders;
    private Long totalReviews;
    private BigDecimal totalSales;
    
    private Long placedOrders;
    private Long confirmedOrders;
    private Long packedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    
    private Long lowStockProducts;
    private Long outOfStockProducts;
}
