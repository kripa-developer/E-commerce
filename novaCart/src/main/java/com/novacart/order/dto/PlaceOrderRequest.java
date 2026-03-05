package com.novacart.order.dto;

import com.novacart.order.domain.Order;
import com.novacart.order.domain.OrderItem;
import com.novacart.order.domain.UserAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// ---- Request DTOs ----

public record PlaceOrderRequest(
        Long addressId,
        AddressDto newAddress,
        @NotBlank String paymentMethod,
        String notes
) {
    public record AddressDto(
            @NotBlank String name,
            @NotBlank String phone,
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String pincode,
            String country,
            String addressType,
            boolean saveAddress
    ) {}
}

// ---- Response DTOs ----
