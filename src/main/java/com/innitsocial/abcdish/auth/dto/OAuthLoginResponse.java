package com.innitsocial.abcdish.auth.dto;

public record OAuthLoginResponse(
        boolean supported,
        String provider,
        String message
) {
}
