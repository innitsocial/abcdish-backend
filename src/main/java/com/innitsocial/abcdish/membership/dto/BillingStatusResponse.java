package com.innitsocial.abcdish.membership.dto;

public record BillingStatusResponse(
        String membershipStatus,
        String planName,
        String currency,
        int monthlyPricePence,
        boolean unlimitedVideos,
        String message
) {
}