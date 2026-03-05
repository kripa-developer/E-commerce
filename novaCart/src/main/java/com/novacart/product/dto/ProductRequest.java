package com.novacart.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        String slug,
        String description,
        String shortDescription,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @DecimalMin("0.01") BigDecimal mrp,
        @NotBlank @Size(max = 100) String brand,
        @NotNull @Min(0) Integer stockQuantity,
        @NotNull Long categoryId,
        String sku,
        Integer weightGrams,
        List<ImageRequest> images,
        List<AttributeRequest> attributes
) {
    public record ImageRequest(
            @NotBlank String imageUrl,
            String altText,
            int displayOrder,
            boolean primary
    ) {}

    public record AttributeRequest(
            @NotBlank String name,
            @NotBlank String value,
            int displayOrder
    ) {}
}
