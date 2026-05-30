package com.innitsocial.abcdish.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        String name,
        @Email String email,
        String mobileNumber,
        @Size(min = 6) String password
) {
}