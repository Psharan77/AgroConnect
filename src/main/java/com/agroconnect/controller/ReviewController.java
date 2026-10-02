package com.agroconnect.controller;

import com.agroconnect.dto.ProductReviewSummaryDto;
import com.agroconnect.dto.ReviewDto;
import com.agroconnect.dto.ReviewRequestDto;
import com.agroconnect.security.SecurityUtils;
import com.agroconnect.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ProductReviewSummaryDto> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequestDto request) {
        return new ResponseEntity<>(reviewService.createReview(securityUtils.getCurrentCustomerId(), productId, request), HttpStatus.CREATED);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto request) {
        return ResponseEntity.ok(reviewService.updateReview(securityUtils.getCurrentCustomerId(), id, request));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(securityUtils.getCurrentCustomerId(), id);
        return ResponseEntity.ok("Review deleted successfully.");
    }
}
