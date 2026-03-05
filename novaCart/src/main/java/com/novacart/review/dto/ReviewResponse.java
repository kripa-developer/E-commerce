package com.novacart.review.dto;

import com.novacart.review.domain.Review;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ReviewResponse(
        Long id,
        Long userId,
        String userEmail,
        Long productId,
        int rating,
        String title,
        String body,
        boolean verifiedPurchase,
        int helpfulCount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getUser().getId(), r.getUser().getEmail(),
                r.getProduct().getId(), r.getRating(), r.getTitle(), r.getBody(),
                r.isVerifiedPurchase(), r.getHelpfulCount(), r.getStatus().name(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
