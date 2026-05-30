package com.innitsocial.abcdish.membership.dto;

public record VideoAccessResponse(
        boolean allowed,
        String message,
        long usedViews,
        long remainingViews,
        String membershipStatus
) {
}