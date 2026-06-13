package com.innitsocial.abcdish.moderation;

public record ModerationResult(
        ModerationStatus status,
        String reason
) {
}
