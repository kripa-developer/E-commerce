package com.novacart.order.dto;

import com.novacart.order.domain.UserAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number") String phone,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Invalid pincode") String pincode,
        String country,
        String addressType,
        boolean defaultAddress
) {}
