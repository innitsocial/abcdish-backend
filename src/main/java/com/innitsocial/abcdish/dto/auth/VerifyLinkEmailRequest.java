package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyLinkEmailRequest(
        @Email @NotBlank String email,
        @NotBlank String otp
) {
}