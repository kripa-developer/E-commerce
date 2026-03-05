package com.novacart.admin.dto;

import com.novacart.order.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentOrderDto(
        Long id,
        String orderNumber,
        String userEmail,
        String status,
        String paymentStatus,
        BigDecimal totalAmount,
        int itemCount,
        Instant createdAt
) {
    public static RecentOrderDto from(Order o) {
        return new RecentOrderDto(
                o.getId(), o.getOrderNumber(), o.getUser().getEmail(),
                o.getStatus().name(), o.getPaymentStatus().name(),
                o.getTotalAmount(), o.getItems().size(), o.getCreatedAt()
        );
    }
}