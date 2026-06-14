package com.innitsocial.abcdish.content.dto;

public record RecipeDraftRequest(
        String sourceType,
        String sourceUrl,
        String titleHint,
        String transcript,
        String creatorName
) {
}
