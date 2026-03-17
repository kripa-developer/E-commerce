package com.novacart.product.dto;

import com.novacart.product.domain.Product;

import java.math.BigDecimal;

public record ProductSummary(
        Long id,
        String name,
        String slug,
        String shortDescription,
        BigDecimal price,
        BigDecimal mrp,
        BigDecimal discountPercent,
        String brand,
        boolean inStock,
        String primaryImageUrl,
        String categoryName,
        BigDecimal averageRating,
        int reviewCount,
        int soldCount
) {
    public static ProductSummary from(Product p) {
        String imageUrl = p.getImages().stream()
                .filter(img -> img.isPrimary())
                .findFirst()
                .or(() -> p.getImages().stream().findFirst())
                .map(img -> img.getImageUrl())
                .orElse(null);

        return new ProductSummary(
                p.getId(), p.getName(), p.getSlug(), p.getShortDescription(),
                p.getPrice(), p.getMrp(), p.getDiscountPercent(),
                p.getBrand(), p.isInStock(), imageUrl,
                p.getCategory().getName(),
                p.getAverageRating(), p.getReviewCount(), p.getSoldCount()
        );
    }
}
