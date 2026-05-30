package com.innitsocial.abcdish.dto.auth;

public record CommunicationPreferencesRequest(
        boolean newsletterSubscribed,
        boolean emailMarketingEnabled,
        boolean smsMarketingEnabled,
        boolean pushNotificationsEnabled
) {
}