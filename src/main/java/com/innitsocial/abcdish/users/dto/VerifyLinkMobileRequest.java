package com.innitsocial.abcdish.users.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyLinkMobileRequest(
        @NotBlank String mobileNumber,
        @NotBlank String otp
) {
}