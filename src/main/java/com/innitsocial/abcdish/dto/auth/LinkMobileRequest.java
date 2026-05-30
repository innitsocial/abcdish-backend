package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LinkMobileRequest(
        @NotBlank String mobileNumber
) {
}