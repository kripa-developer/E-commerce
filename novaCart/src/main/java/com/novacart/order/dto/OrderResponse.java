package com.novacart.order.dto;

import com.novacart.order.domain.Order;
import com.novacart.order.domain.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String status,
        String paymentStatus,
        String paymentMethod,
        String paymentId,
        ShippingAddressDto shippingAddress,
        List<OrderItemDto> items,
        BigDecimal subtotal,
        BigDecimal shippingCharge,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String notes,
        String cancelledReason,
        Instant expectedDeliveryDate,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt
) {
    public record ShippingAddressDto(
            String name, String phone, String line1, String line2,
            String city, String state, String pincode, String country
    ) {}

    public record OrderItemDto(
            Long id,
            Long productId,
            String productName,
            String productSku,
            String productImageUrl,
            BigDecimal unitPrice,
            BigDecimal mrp,
            int quantity,
            BigDecimal lineTotal
    ) {
        public static OrderItemDto from(OrderItem item) {
            return new OrderItemDto(item.getId(), item.getProduct().getId(),
                    item.getProductName(), item.getProductSku(), item.getProductImageUrl(),
                    item.getUnitPrice(), item.getMrp(), item.getQuantity(), item.getLineTotal());
        }
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getStatus().name(), o.getPaymentStatus().name(),
                o.getPaymentMethod(), o.getPaymentId(),
                new ShippingAddressDto(o.getShippingName(), o.getShippingPhone(),
                        o.getShippingLine1(), o.getShippingLine2(), o.getShippingCity(),
                        o.getShippingState(), o.getShippingPincode(), o.getShippingCountry()),
                o.getItems().stream().map(OrderItemDto::from).toList(),
                o.getSubtotal(), o.getShippingCharge(), o.getDiscountAmount(), o.getTotalAmount(),
                o.getNotes(), o.getCancelledReason(),
                o.getExpectedDeliveryDate(), o.getDeliveredAt(),
                o.getCreatedAt(), o.getUpdatedAt()
        );
    }
}
