package com.agroconnect.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FarmerDashboardDto {
    private Long totalProducts;
    private Integer availableStock;
    private Long lowStockProductsCount;
    
    private Long totalOrders;
    private Long pendingOrders; // PLACED
    private Long confirmedOrders;
    private Long packedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    
    private BigDecimal totalSales;
    private BigDecimal monthlySales;
    
    private List<OrderDto> recentOrders;
    private List<ProductDto> lowStockProducts;
}
