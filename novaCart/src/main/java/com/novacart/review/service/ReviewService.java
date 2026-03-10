package com.novacart.review.service;

import com.novacart.product.domain.Product;
import com.novacart.product.repository.ProductRepository;
import com.novacart.review.domain.Review;
import com.novacart.review.domain.ReviewStatus;
import com.novacart.review.dto.ProductReviewSummary;
import com.novacart.review.dto.ReviewRequest;
import com.novacart.review.dto.ReviewResponse;
import com.novacart.review.repository.ReviewRepository;
import com.novacart.user.domain.User;
import com.novacart.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, int page, int size, String sortBy) {
        Sort sort = "helpful".equals(sortBy)
                ? Sort.by(Sort.Direction.DESC, "helpfulCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                        productId, ReviewStatus.PUBLISHED, PageRequest.of(page, size, sort))
                .map(ReviewResponse::from);
    }

    public ProductReviewSummary getProductReviewSummary(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        int count = reviewRepository.countPublishedByProductId(productId);
        var rawDist = reviewRepository.findRatingDistributionByProductId(productId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        for (Object[] row : rawDist) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        return new ProductReviewSummary(avg != null ? avg : 0.0, count, distribution);
    }

    @Transactional
    public ReviewResponse createReview(String email, Long productId, ReviewRequest request) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));

        if (reviewRepository.existsByUserAndProductId(user, productId)) {
            throw new ResponseStatusException(CONFLICT, "You have already reviewed this product");
        }

        Review review = new Review(user, product, request.rating(), request.title(), request.body(), false);
        Review saved = reviewRepository.save(review);
        updateProductRating(product);
        return ReviewResponse.from(saved);
    }

    @Transactional
    public ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request) {
        User user = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You can only edit your own reviews");
        }

        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setBody(request.body());

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct());
        return ReviewResponse.from(saved);
    }

    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You can only delete your own reviews");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    @Transactional
    public void markHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review not found"));
        review.incrementHelpful();
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(String email, int page, int size) {
        User user = getUser(email);
        return reviewRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size))
                .map(ReviewResponse::from);
    }

    private void updateProductRating(Product product) {
        Double avg = reviewRepository.findAverageRatingByProductId(product.getId());
        int count = reviewRepository.countPublishedByProductId(product.getId());
        BigDecimal avgRating = avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        product.updateRating(avgRating, count);
        productRepository.save(product);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }
}
