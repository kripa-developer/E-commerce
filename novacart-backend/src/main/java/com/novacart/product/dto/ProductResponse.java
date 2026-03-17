package com.novacart.product.dto;

import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductAttribute;
import com.novacart.product.domain.ProductImage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        String shortDescription,
        BigDecimal price,
        BigDecimal mrp,
        BigDecimal discountPercent,
        String brand,
        int stockQuantity,
        boolean inStock,
        Long categoryId,
        String categoryName,
        String categorySlug,
        List<ImageDto> images,
        List<AttributeDto> attributes,
        String status,
        BigDecimal averageRating,
        int reviewCount,
        int soldCount,
        String sku,
        Integer weightGrams,
        Instant createdAt,
        Instant updatedAt
) {
    public record ImageDto(Long id, String imageUrl, String altText, int displayOrder, boolean primary) {
        public static ImageDto from(ProductImage img) {
            return new ImageDto(img.getId(), img.getImageUrl(), img.getAltText(), img.getDisplayOrder(), img.isPrimary());
        }
    }

    public record AttributeDto(Long id, String name, String value, int displayOrder) {
        public static AttributeDto from(ProductAttribute attr) {
            return new AttributeDto(attr.getId(), attr.getName(), attr.getValue(), attr.getDisplayOrder());
        }
    }

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getShortDescription(),
                p.getPrice(), p.getMrp(), p.getDiscountPercent(),
                p.getBrand(), p.getStockQuantity(), p.isInStock(),
                p.getCategory().getId(), p.getCategory().getName(), p.getCategory().getSlug(),
                p.getImages().stream().map(ImageDto::from).toList(),
                p.getAttributes().stream().map(AttributeDto::from).toList(),
                p.getStatus().name(),
                p.getAverageRating(), p.getReviewCount(), p.getSoldCount(),
                p.getSku(), p.getWeightGrams(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
