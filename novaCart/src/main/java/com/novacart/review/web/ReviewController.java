package com.novacart.review.web;

import com.novacart.review.dto.ProductReviewSummary;
import com.novacart.review.dto.ReviewRequest;
import com.novacart.review.dto.ReviewResponse;
import com.novacart.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size, sortBy));
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ProductReviewSummary> getReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviewSummary(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            Authentication auth,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(201).body(reviewService.createReview(auth.getName(), productId, request));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            Authentication auth,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(auth.getName(), reviewId, request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(Authentication auth, @PathVariable Long reviewId) {
        reviewService.deleteReview(auth.getName(), reviewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Void> markHelpful(@PathVariable Long reviewId) {
        reviewService.markHelpful(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/me")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getMyReviews(auth.getName(), page, size));
    }
}
