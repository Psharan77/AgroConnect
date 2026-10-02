package com.agroconnect.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummaryDto {
    private List<ReviewDto> reviews;
    private Double averageRating;
    private Long totalReviews;
}
