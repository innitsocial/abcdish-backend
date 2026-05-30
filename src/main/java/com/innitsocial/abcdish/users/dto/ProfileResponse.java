package com.innitsocial.abcdish.users.dto;

public record ProfileResponse(
        Long userId,
        String name,
        String email,
        String mobileNumber,
        boolean emailVerified,
        boolean mobileVerified,
        String membershipStatus,
        int monthlyVideoViews,
        boolean newsletterSubscribed,
        boolean emailMarketingEnabled,
        boolean smsMarketingEnabled,
        boolean pushNotificationsEnabled
) {
}