package com.innitsocial.abcdish.dto;

public record VideoAccessResponse(
        boolean allowed,
        String message,
        long usedViews,
        long remainingViews,
        String membershipStatus
) {
}