package com.innitsocial.abcdish.content.dto;

import java.util.List;

public record RecipeDraftResponse(
        String title,
        String description,
        String imageUrl,
        String videoUrl,
        String trailerUrl,
        String trailerType,
        String promoTrailerTitle,
        String promoTrailerSubtitle,
        Integer duration,
        String complexity,
        String affordability,
        List<String> categories,
        List<String> ingredients,
        List<String> steps,
        boolean glutenFree,
        boolean lactoseFree,
        boolean vegan,
        boolean vegetarian,
        String extractionStatus,
        String extractionMessage
) {
}
