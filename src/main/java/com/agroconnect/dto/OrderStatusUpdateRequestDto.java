package com.agroconnect.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateRequestDto {
    @NotBlank(message = "Status cannot be blank")
    private String status;
}
