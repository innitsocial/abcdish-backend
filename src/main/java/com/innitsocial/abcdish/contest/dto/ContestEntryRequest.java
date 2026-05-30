package com.innitsocial.abcdish.contest.dto;

import jakarta.validation.constraints.NotBlank;

public record ContestEntryRequest(
        @NotBlank String title,
        String description,
        @NotBlank String videoUrl,
        String thumbnailUrl
) {
}
