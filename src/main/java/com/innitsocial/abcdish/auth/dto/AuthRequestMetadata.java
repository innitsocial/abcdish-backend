package com.innitsocial.abcdish.auth.dto;

public record AuthRequestMetadata(
        String deviceName,
        String ipAddress
) {
}