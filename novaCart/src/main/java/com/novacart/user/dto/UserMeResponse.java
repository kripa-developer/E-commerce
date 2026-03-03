package com.novacart.user.dto;

public record UserMeResponse(
        Long id,
        String email,
        String role,
        boolean enabled
) {
}
