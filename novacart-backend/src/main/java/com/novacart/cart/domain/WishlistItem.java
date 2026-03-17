package com.novacart.cart.domain;

import com.novacart.product.domain.Product;
import com.novacart.user.domain.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected WishlistItem() {}

    public WishlistItem(User user, Product product) {
        this.user = user;
        this.product = product;
        this.addedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Product getProduct() { return product; }
    public Instant getAddedAt() { return addedAt; }
}
