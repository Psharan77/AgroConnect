package com.agroconnect.service.impl;

import com.agroconnect.dto.ProductReviewSummaryDto;
import com.agroconnect.dto.ReviewDto;
import com.agroconnect.dto.ReviewRequestDto;
import com.agroconnect.entity.Customer;
import com.agroconnect.entity.Product;
import com.agroconnect.entity.Review;
import com.agroconnect.exception.BadRequestException;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.CustomerRepository;
import com.agroconnect.repository.OrderItemRepository;
import com.agroconnect.repository.ProductRepository;
import com.agroconnect.repository.ReviewRepository;
import com.agroconnect.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public ReviewDto createReview(Long customerId, Long productId, ReviewRequestDto request) {
        // Enforce Rule 1: Must have purchased the product AND order is DELIVERED
        boolean hasPurchasedAndDelivered = orderItemRepository.existsByOrder_Customer_IdAndProduct_IdAndOrder_Status(customerId, productId, "DELIVERED");
        if (!hasPurchasedAndDelivered) {
            throw new BadRequestException("You can only review products you have purchased and received (order must be DELIVERED).");
        }

        // Enforce Rule 2: Cannot review multiple times
        if (reviewRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            throw new BadRequestException("You have already reviewed this product.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToDto(reviewRepository.save(review));
    }

    @Override
    public ProductReviewSummaryDto getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        List<ReviewDto> dtos = reviews.stream().map(this::mapToDto).collect(Collectors.toList());
        
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        if (avgRating == null) {
            avgRating = 0.0;
        }
        
        // Round to 1 decimal place (e.g. 4.5)
        Double roundedAvg = Math.round(avgRating * 10.0) / 10.0;
        
        return new ProductReviewSummaryDto(dtos, roundedAvg, (long) reviews.size());
    }

    @Override
    public ReviewDto updateReview(Long customerId, Long reviewId, ReviewRequestDto request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You are not authorized to update this review.");
        }
        
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        return mapToDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Long customerId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You are not authorized to delete this review.");
        }
        
        reviewRepository.delete(review);
    }

    private ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setProductId(review.getProduct().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
