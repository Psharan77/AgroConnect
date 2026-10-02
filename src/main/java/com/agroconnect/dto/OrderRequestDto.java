package com.agroconnect.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDto {
    @NotNull(message = "Address ID is required to place an order")
    private Long addressId;
}
