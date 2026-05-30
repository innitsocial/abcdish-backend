package com.innitsocial.abcdish.dto;

import com.innitsocial.abcdish.entity.Meal;

import java.util.List;

public record MealResponseDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        String videoUrl,
        Integer duration,
        String complexity,
        String affordability,
        List<String> categories,
        List<String> ingredients,
        List<String> steps,
        boolean glutenFree,
        boolean lactoseFree,
        boolean vegan,
        boolean vegetarian
) {
    public static MealResponseDto fromEntity(Meal meal) {
        return new MealResponseDto(
                meal.getId(),
                meal.getTitle(),
                meal.getDescription(),
                meal.getImageUrl(),
                meal.getVideoUrl(),
                meal.getDuration(),
                meal.getComplexity(),
                meal.getAffordability(),
                meal.getCategories(),
                meal.getIngredients(),
                meal.getSteps(),
                meal.isGlutenFree(),
                meal.isLactoseFree(),
                meal.isVegan(),
                meal.isVegetarian()
        );
    }
}