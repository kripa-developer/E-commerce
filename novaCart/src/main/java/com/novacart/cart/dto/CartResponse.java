package com.novacart.cart.dto;

import com.novacart.cart.domain.Cart;
import com.novacart.cart.domain.CartItem;
import com.novacart.cart.domain.WishlistItem;
import com.novacart.product.dto.ProductSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemDto> items,
        int totalItems,
        BigDecimal subtotal,
        Instant updatedAt
) {
    public record CartItemDto(
            Long id,
            Long productId,
            String productName,
            String productSlug,
            String productImageUrl,
            String brand,
            BigDecimal unitPrice,
            BigDecimal mrp,
            int quantity,
            BigDecimal lineTotal,
            boolean inStock,
            int availableStock
    ) {
        public static CartItemDto from(CartItem item) {
            String imageUrl = item.getProduct().getImages().stream()
                    .filter(img -> img.isPrimary()).findFirst()
                    .or(() -> item.getProduct().getImages().stream().findFirst())
                    .map(img -> img.getImageUrl()).orElse(null);

            return new CartItemDto(
                    item.getId(), item.getProduct().getId(),
                    item.getProduct().getName(), item.getProduct().getSlug(),
                    imageUrl, item.getProduct().getBrand(),
                    item.getUnitPrice(), item.getProduct().getMrp(),
                    item.getQuantity(), item.getLineTotal(),
                    item.getProduct().isInStock(), item.getProduct().getStockQuantity()
            );
        }
    }

    public static CartResponse from(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream().map(CartItemDto::from).toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItemDto::quantity).sum();

        return new CartResponse(cart.getId(), items, totalItems, subtotal, cart.getUpdatedAt());
    }
}
