package com.innitsocial.abcdish.dto.auth;

public record AuthRequestMetadata(
        String deviceName,
        String ipAddress
) {
}