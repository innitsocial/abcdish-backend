package com.innitsocial.abcdish.contest.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ContestEntryRequest(
        @NotBlank String title,
        String description,
        @NotBlank String videoUrl,
        String thumbnailUrl,
        Integer duration,
        String complexity,
        String competitionCategory,
        List<String> categories,
        List<String> ingredients,
        List<String> steps,
        boolean glutenFree,
        boolean lactoseFree,
        boolean vegan,
        boolean vegetarian,
        boolean soundFreeConfirmed,
        boolean aiNarrationRequested
) {
}
