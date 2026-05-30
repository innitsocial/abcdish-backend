package com.innitsocial.abcdish.membership.dto;

public record CreateCheckoutSessionResponse(
        String checkoutUrl,
        String message
) {
}