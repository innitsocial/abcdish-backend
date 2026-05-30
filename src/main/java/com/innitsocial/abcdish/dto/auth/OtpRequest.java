package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank String destination
) {
}