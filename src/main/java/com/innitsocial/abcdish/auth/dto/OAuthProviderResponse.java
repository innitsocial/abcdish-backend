package com.innitsocial.abcdish.auth.dto;

public record OAuthProviderResponse(
        String provider,
        String displayName,
        String authorizationUrl,
        boolean enabled
) {
}
