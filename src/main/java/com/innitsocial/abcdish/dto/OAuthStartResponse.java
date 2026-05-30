package com.innitsocial.abcdish.dto;

public record OAuthStartResponse(
        String provider,
        String authorizationUrl,
        String message
) {
}