package com.innitsocial.abcdish.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequest(
        @Size(min = 6) @NotBlank String password
) {
}