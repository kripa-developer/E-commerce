package com.novacart.product.domain;

import com.novacart.category.domain.Category;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "mrp", precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(nullable = false)
    private String brand;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "sold_count", nullable = false)
    private int soldCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Product() {}

    public Product(String name, String slug, String description, String shortDescription,
                   BigDecimal price, BigDecimal mrp, String brand, int stockQuantity,
                   Category category, String sku, Integer weightGrams) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.shortDescription = shortDescription;
        this.price = price;
        this.mrp = mrp;
        this.brand = brand;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.sku = sku;
        this.weightGrams = weightGrams;
        recalculateDiscount();
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void recalculateDiscount() {
        if (mrp != null && mrp.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            BigDecimal discount = mrp.subtract(price)
                    .divide(mrp, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            this.discountPercent = discount.max(BigDecimal.ZERO);
        }
    }

    public void updateRating(BigDecimal newAverage, int count) {
        this.averageRating = newAverage;
        this.reviewCount = count;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + name);
        }
        this.stockQuantity -= quantity;
        this.soldCount += quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public boolean isInStock() {
        return stockQuantity > 0 && status == ProductStatus.ACTIVE;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getShortDescription() { return shortDescription; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMrp() { return mrp; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public String getBrand() { return brand; }
    public int getStockQuantity() { return stockQuantity; }
    public int getSoldCount() { return soldCount; }
    public Category getCategory() { return category; }
    public List<ProductImage> getImages() { return images; }
    public List<ProductAttribute> getAttributes() { return attributes; }
    public ProductStatus getStatus() { return status; }
    public BigDecimal getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public String getSku() { return sku; }
    public Integer getWeightGrams() { return weightGrams; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public void setPrice(BigDecimal price) { this.price = price; recalculateDiscount(); }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; recalculateDiscount(); }
    public void setBrand(String brand) { this.brand = brand; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setCategory(Category category) { this.category = category; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public void setSku(String sku) { this.sku = sku; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }
}
