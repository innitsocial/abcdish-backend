package com.innitsocial.abcdish.users.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkMobileRequest(
        @NotBlank String mobileNumber
) {
}