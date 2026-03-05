package com.novacart.cart.dto;

import com.novacart.cart.domain.WishlistItem;
import com.novacart.product.dto.ProductSummary;

import java.time.Instant;
import java.util.List;

public record WishlistResponse(
        List<WishlistItemDto> items,
        int totalItems
) {
    public record WishlistItemDto(
            Long id,
            ProductSummary product,
            Instant addedAt
    ) {
        public static WishlistItemDto from(WishlistItem item) {
            return new WishlistItemDto(item.getId(), ProductSummary.from(item.getProduct()), item.getAddedAt());
        }
    }

    public static WishlistResponse from(List<WishlistItem> items) {
        List<WishlistItemDto> dtos = items.stream().map(WishlistItemDto::from).toList();
        return new WishlistResponse(dtos, dtos.size());
    }
}
