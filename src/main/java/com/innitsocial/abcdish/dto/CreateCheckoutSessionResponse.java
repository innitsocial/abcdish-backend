package com.innitsocial.abcdish.dto;

public record CreateCheckoutSessionResponse(
        String checkoutUrl,
        String message
) {
}