package com.agroconnect.service.impl;

import com.agroconnect.dto.FarmerDashboardDto;
import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.OrderItemDto;
import com.agroconnect.entity.Order;
import com.agroconnect.repository.OrderItemRepository;
import com.agroconnect.repository.OrderRepository;
import com.agroconnect.repository.ProductRepository;
import com.agroconnect.service.FarmerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmerDashboardServiceImpl implements FarmerDashboardService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    @Override
    public FarmerDashboardDto getDashboardStats(Long farmerId) {
        FarmerDashboardDto dto = new FarmerDashboardDto();

        // Product stats
        dto.setTotalProducts(productRepository.countByFarmerId(farmerId));
        dto.setAvailableStock(productRepository.sumAvailableStockByFarmerId(farmerId));
        dto.setLowStockProductsCount(productRepository.countByFarmerIdAndQuantityLessThanEqual(farmerId, 5));
        
        List<com.agroconnect.entity.Product> lowStock = productRepository.findByFarmerIdAndQuantityLessThanEqual(farmerId, 5);
        dto.setLowStockProducts(lowStock.stream().map(this::mapProductToDto).collect(Collectors.toList()));

        // Order counts
        dto.setTotalOrders(orderItemRepository.countDistinctOrdersByFarmerId(farmerId));
        dto.setPendingOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "PLACED"));
        dto.setConfirmedOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "CONFIRMED"));
        dto.setPackedOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "PACKED"));
        dto.setShippedOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "SHIPPED"));
        dto.setDeliveredOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "DELIVERED"));
        dto.setCancelledOrders(orderItemRepository.countDistinctOrdersByFarmerIdAndStatus(farmerId, "CANCELLED"));

        // Sales logic
        dto.setTotalSales(orderItemRepository.calculateTotalSalesByFarmerId(farmerId));

        // Monthly bounds
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        dto.setMonthlySales(orderItemRepository.calculateSalesByFarmerIdAndDateRange(farmerId, startOfMonth, endOfMonth));

        // Recent Orders (Top 5)
        List<Order> recentOrders = orderRepository.findRecentOrdersByFarmerId(farmerId, PageRequest.of(0, 5));
        
        dto.setRecentOrders(recentOrders.stream().map(this::mapOrderToDto).collect(Collectors.toList()));

        return dto;
    }

    private com.agroconnect.dto.ProductDto mapProductToDto(com.agroconnect.entity.Product product) {
        com.agroconnect.dto.ProductDto dto = new com.agroconnect.dto.ProductDto();
        dto.setId(product.getId());
        dto.setFarmerId(product.getFarmer() != null ? product.getFarmer().getId() : null);
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setUnit(product.getUnit());
        return dto;
    }

    private OrderDto mapOrderToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setAddressId(order.getAddress().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
