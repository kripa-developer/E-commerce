package com.novacart.review.repository;

import com.novacart.review.domain.Review;
import com.novacart.review.domain.ReviewStatus;
import com.novacart.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Optional<Review> findByUserAndProductId(User user, Long productId);

    boolean existsByUserAndProductId(User user, Long productId);

    Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'PUBLISHED'")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'PUBLISHED'")
    int countPublishedByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT r.rating, COUNT(r) FROM Review r
            WHERE r.product.id = :productId AND r.status = 'PUBLISHED'
            GROUP BY r.rating ORDER BY r.rating DESC
            """)
    java.util.List<Object[]> findRatingDistributionByProductId(@Param("productId") Long productId);
}
