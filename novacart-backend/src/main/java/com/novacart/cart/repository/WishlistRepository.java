package com.novacart.cart.repository;

import com.novacart.cart.domain.WishlistItem;
import com.novacart.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserOrderByAddedAtDesc(User user);
    Optional<WishlistItem> findByUserAndProductId(User user, Long productId);
    boolean existsByUserAndProductId(User user, Long productId);
    void deleteByUserAndProductId(User user, Long productId);
}
