package com.innitsocial.abcdish.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank String idToken,
        String accessToken,
        String deviceName
) {
}
