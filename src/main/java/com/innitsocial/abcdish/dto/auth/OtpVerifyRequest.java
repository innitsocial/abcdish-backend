package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank String destination,
        @NotBlank String otp
) {
}