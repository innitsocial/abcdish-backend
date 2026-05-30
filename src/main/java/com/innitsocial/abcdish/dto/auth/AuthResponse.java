package com.innitsocial.abcdish.dto.auth;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String name,
        String email,
        String mobileNumber,
        boolean emailVerified,
        boolean mobileVerified,
        String membershipStatus
) {
}