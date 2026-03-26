package com.novacart.review.service;

import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductStatus;
import com.novacart.product.repository.ProductRepository;
import com.novacart.review.domain.Review;
import com.novacart.review.domain.ReviewStatus;
import com.novacart.review.dto.ProductReviewSummary;
import com.novacart.review.dto.ReviewRequest;
import com.novacart.review.repository.ReviewRepository;
import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Tests")
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private User otherUser;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        user = spy(new User("user@novacart.com", "$2a$10$hash", UserRole.CUSTOMER, true));
        doReturn(1L).when(user).getId();
        otherUser = spy(new User("other@novacart.com", "$2a$10$hash2", UserRole.CUSTOMER, true));
        doReturn(2L).when(otherUser).getId();
        product = new Product("iPhone 17", "iphone-17", "desc", "short",
                new BigDecimal("112000"), new BigDecimal("114000"), "Apple", 10, null, "SKU001", 200);
        review = new Review(user, product, 5, "Great product", "Really loved it", false);
    }

    // ════════════════════════════════════════════════
    // GET PRODUCT REVIEWS
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("getProductReviews()")
    class GetProductReviews {

        @Test
        @DisplayName("should return page of reviews sorted by recent")
        void shouldReturnReviewsSortedByRecent() {
            when(reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                    eq(1L), eq(ReviewStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(review)));

            var result = reviewService.getProductReviews(1L, 0, 10, "recent");

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(reviewRepository).findByProductIdAndStatusOrderByCreatedAtDesc(
                    eq(1L), eq(ReviewStatus.PUBLISHED), any(Pageable.class));
        }

        @Test
        @DisplayName("should return empty page when no reviews exist")
        void shouldReturnEmptyPageWhenNoReviews() {
            when(reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                    eq(1L), eq(ReviewStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            var result = reviewService.getProductReviews(1L, 0, 10, "recent");

            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // ════════════════════════════════════════════════
    // GET REVIEW SUMMARY
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("getProductReviewSummary()")
    class GetProductReviewSummary {

        @Test
        @DisplayName("should return summary with correct average and count")
        void shouldReturnSummaryWithCorrectValues() {
            when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(4.5);
            when(reviewRepository.countPublishedByProductId(1L)).thenReturn(10);
            when(reviewRepository.findRatingDistributionByProductId(1L))
                    .thenReturn(List.of(new Object[]{5, 6L}, new Object[]{4, 4L}));

            ProductReviewSummary summary = reviewService.getProductReviewSummary(1L);

            assertThat(summary.averageRating()).isEqualTo(4.5);
            assertThat(summary.totalReviews()).isEqualTo(10);
            assertThat(summary.ratingDistribution().get(5)).isEqualTo(6L);
            assertThat(summary.ratingDistribution().get(4)).isEqualTo(4L);
            assertThat(summary.ratingDistribution().get(1)).isEqualTo(0L); // missing ratings default to 0
        }

        @Test
        @DisplayName("should return 0.0 average when no reviews")
        void shouldReturn0AverageWhenNoReviews() {
            when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(null);
            when(reviewRepository.countPublishedByProductId(1L)).thenReturn(0);
            when(reviewRepository.findRatingDistributionByProductId(1L)).thenReturn(List.of());

            ProductReviewSummary summary = reviewService.getProductReviewSummary(1L);

            assertThat(summary.averageRating()).isEqualTo(0.0);
            assertThat(summary.totalReviews()).isEqualTo(0);
        }
    }

    // ════════════════════════════════════════════════
    // CREATE REVIEW
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("createReview()")
    class CreateReview {

        @Test
        @DisplayName("should create review successfully")
        void shouldCreateReviewSuccessfully() {
            ReviewRequest request = new ReviewRequest(5, "Great!", "Loved it");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(reviewRepository.existsByUserAndProductId(user, 1L)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewRepository.findAverageRatingByProductId(product.getId())).thenReturn(5.0);
            when(reviewRepository.countPublishedByProductId(product.getId())).thenReturn(1);

            var result = reviewService.createReview("user@novacart.com", 1L, request);

            assertThat(result).isNotNull();
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("should throw 404 when product not found")
        void shouldThrow404WhenProductNotFound() {
            ReviewRequest request = new ReviewRequest(5, "Great!", "Loved it");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview("user@novacart.com", 99L, request));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getReason()).isEqualTo("Product not found");
        }

        @Test
        @DisplayName("should throw 409 when user already reviewed product")
        void shouldThrow409WhenAlreadyReviewed() {
            ReviewRequest request = new ReviewRequest(5, "Great!", "Loved it");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(reviewRepository.existsByUserAndProductId(user, 1L)).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview("user@novacart.com", 1L, request));

            assertThat(ex.getStatusCode().value()).isEqualTo(409);
            assertThat(ex.getReason()).isEqualTo("You have already reviewed this product");
        }

        @Test
        @DisplayName("should throw 404 when user not found")
        void shouldThrow404WhenUserNotFound() {
            ReviewRequest request = new ReviewRequest(5, "Great!", "Loved it");
            when(userRepository.findByEmail("unknown@novacart.com")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview("unknown@novacart.com", 1L, request));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getReason()).isEqualTo("User not found");
        }
    }

    // ════════════════════════════════════════════════
    // UPDATE REVIEW
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("updateReview()")
    class UpdateReview {

        @Test
        @DisplayName("should update review successfully")
        void shouldUpdateReviewSuccessfully() {
            ReviewRequest request = new ReviewRequest(4, "Updated title", "Updated body");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
            when(reviewRepository.save(review)).thenReturn(review);
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(4.0);
            when(reviewRepository.countPublishedByProductId(any())).thenReturn(1);

            // Set user id via reflection workaround — use same user reference
            var result = reviewService.updateReview("user@novacart.com", 1L, request);

            assertThat(result).isNotNull();
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("should throw 404 when review not found")
        void shouldThrow404WhenReviewNotFound() {
            ReviewRequest request = new ReviewRequest(4, "Title", "Body");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.updateReview("user@novacart.com", 99L, request));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // DELETE REVIEW
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {

        @Test
        @DisplayName("should delete review successfully")
        void shouldDeleteReviewSuccessfully() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(0.0);
            when(reviewRepository.countPublishedByProductId(any())).thenReturn(0);

            assertDoesNotThrow(() -> reviewService.deleteReview("user@novacart.com", 1L));

            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("should throw 404 when review not found")
        void shouldThrow404WhenReviewNotFound() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.deleteReview("user@novacart.com", 99L));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // MARK HELPFUL
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("markHelpful()")
    class MarkHelpful {

        @Test
        @DisplayName("should increment helpful count")
        void shouldIncrementHelpfulCount() {
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
            int before = review.getHelpfulCount();

            reviewService.markHelpful(1L);

            assertThat(review.getHelpfulCount()).isEqualTo(before + 1);
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("should throw 404 when review not found")
        void shouldThrow404WhenReviewNotFound() {
            when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> reviewService.markHelpful(99L));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }
}