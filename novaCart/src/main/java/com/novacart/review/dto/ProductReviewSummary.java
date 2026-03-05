package com.novacart.review.dto;

import java.util.Map;

public record ProductReviewSummary(
        double averageRating,
        int totalReviews,
        Map<Integer, Long> ratingDistribution
) {}
