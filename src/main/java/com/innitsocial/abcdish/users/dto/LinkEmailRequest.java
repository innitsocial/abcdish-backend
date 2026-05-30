package com.innitsocial.abcdish.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LinkEmailRequest(
        @Email @NotBlank String email
) {
}