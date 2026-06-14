package com.innitsocial.abcdish.content.dto;

import com.innitsocial.abcdish.content.entity.Meal;

import java.util.List;

public record MealResponseDto(
        Long id,
        String recipeCode,
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
        String moderationStatus,
        String moderationReason
) {
    public static MealResponseDto fromEntity(Meal meal) {
        return new MealResponseDto(
                meal.getId(),
                recipeCodeFor(meal),
                meal.getTitle(),
                meal.getDescription(),
                meal.getImageUrl(),
                meal.getVideoUrl(),
                meal.getTrailerUrl(),
                meal.getTrailerType(),
                meal.getPromoTrailerTitle(),
                meal.getPromoTrailerSubtitle(),
                meal.getDuration(),
                meal.getComplexity(),
                meal.getAffordability(),
                meal.getCategories(),
                meal.getIngredients(),
                meal.getSteps(),
                meal.isGlutenFree(),
                meal.isLactoseFree(),
                meal.isVegan(),
                meal.isVegetarian(),
                meal.getModerationStatus() == null ? "APPROVED" : meal.getModerationStatus().name(),
                meal.getModerationReason()
        );
    }

    private static String recipeCodeFor(Meal meal) {
        if (meal.getRecipeCode() != null && !meal.getRecipeCode().isBlank()) {
            return meal.getRecipeCode();
        }

        return meal.getId() == null ? "" : String.valueOf(10000 + meal.getId());
    }
}
