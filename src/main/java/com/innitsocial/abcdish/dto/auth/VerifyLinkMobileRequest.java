package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyLinkMobileRequest(
        @NotBlank String mobileNumber,
        @NotBlank String otp
) {
}