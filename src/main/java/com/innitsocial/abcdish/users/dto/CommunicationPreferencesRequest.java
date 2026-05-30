package com.innitsocial.abcdish.users.dto;

public record CommunicationPreferencesRequest(
        boolean newsletterSubscribed,
        boolean emailMarketingEnabled,
        boolean smsMarketingEnabled,
        boolean pushNotificationsEnabled
) {
}