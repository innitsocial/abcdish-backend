package com.innitsocial.abcdish.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank String destination,
        @NotBlank String otp
) {
}