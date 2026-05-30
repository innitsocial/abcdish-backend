package com.innitsocial.abcdish.auth.service;

import com.innitsocial.abcdish.auth.dto.OAuthLoginResponse;
import com.innitsocial.abcdish.auth.dto.OAuthProviderResponse;
import com.innitsocial.abcdish.auth.dto.OAuthStartResponse;
import com.innitsocial.abcdish.auth.oauth.OAuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class OAuthService {

    @Value("${app.oauth.base-authorization-url:/oauth2/authorization}")
    private String baseAuthorizationUrl;

    public List<OAuthProviderResponse> getProviders() {
        return Arrays.stream(OAuthProvider.values())
                .map(provider -> new OAuthProviderResponse(
                        provider.name(),
                        displayName(provider),
                        authorizationUrl(provider),
                        true
                ))
                .toList();
    }

    public OAuthStartResponse start(OAuthProvider provider) {
        return new OAuthStartResponse(
                provider.name(),
                authorizationUrl(provider),
                displayName(provider) + " OAuth flow is available when provider client credentials are configured."
        );
    }


    public OAuthLoginResponse login(OAuthProvider provider) {
        return new OAuthLoginResponse(
                false,
                provider.name(),
                "OAuth token verification is not configured yet. Configure provider client credentials and token verification before enabling production OAuth login."
        );
    }

    private String authorizationUrl(OAuthProvider provider) {
        return baseAuthorizationUrl + "/" + provider.name().toLowerCase();
    }

    private String displayName(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "Google";
            case APPLE -> "Apple";
            case MICROSOFT -> "Microsoft";
            case FACEBOOK -> "Facebook";
        };
    }
}
