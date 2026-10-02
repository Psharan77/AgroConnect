package com.agroconnect.service;

import com.agroconnect.dto.ProductReviewSummaryDto;
import com.agroconnect.dto.ReviewDto;
import com.agroconnect.dto.ReviewRequestDto;

public interface ReviewService {
    ReviewDto createReview(Long customerId, Long productId, ReviewRequestDto request);
    ProductReviewSummaryDto getProductReviews(Long productId);
    ReviewDto updateReview(Long customerId, Long reviewId, ReviewRequestDto request);
    void deleteReview(Long customerId, Long reviewId);
}
