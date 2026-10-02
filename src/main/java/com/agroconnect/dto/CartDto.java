package com.agroconnect.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private Long customerId;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;
}
