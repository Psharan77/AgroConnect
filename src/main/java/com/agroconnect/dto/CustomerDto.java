package com.agroconnect.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerDto {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
