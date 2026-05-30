package com.innitsocial.abcdish.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank String destination
) {
}