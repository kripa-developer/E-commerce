package com.novacart.order.dto;

import com.novacart.order.domain.UserAddress;

import java.time.Instant;

public record AddressResponse(
        Long id,
        String name,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        String country,
        String addressType,
        boolean defaultAddress,
        Instant createdAt
) {
    public static AddressResponse from(UserAddress a) {
        return new AddressResponse(a.getId(), a.getName(), a.getPhone(),
                a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPincode(), a.getCountry(), a.getAddressType(),
                a.isDefaultAddress(), a.getCreatedAt());
    }
}
