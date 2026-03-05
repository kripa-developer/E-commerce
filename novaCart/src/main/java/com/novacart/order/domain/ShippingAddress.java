package com.novacart.order.domain;

public record ShippingAddress(
        String name,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        String country
) {}
