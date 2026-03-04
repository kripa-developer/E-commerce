package com.novacart.auth.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresInMs
) {
}
