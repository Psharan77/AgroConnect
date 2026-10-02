package com.agroconnect.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private Long customerId;
    private String customerName; // Helpful for displaying reviews on the frontend
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
