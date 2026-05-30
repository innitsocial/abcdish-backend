package com.innitsocial.abcdish.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String otp,
        @Size(min = 6) @NotBlank String newPassword
) {
}