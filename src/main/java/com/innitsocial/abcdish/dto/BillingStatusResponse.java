package com.innitsocial.abcdish.dto;

public record BillingStatusResponse(
        String membershipStatus,
        String planName,
        String currency,
        int monthlyPricePence,
        boolean unlimitedVideos,
        String message
) {
}