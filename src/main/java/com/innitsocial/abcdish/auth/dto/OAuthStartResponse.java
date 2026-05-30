package com.innitsocial.abcdish.auth.dto;

public record OAuthStartResponse(
        String provider,
        String authorizationUrl,
        String message
) {
}