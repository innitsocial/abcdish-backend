package com.innitsocial.abcdish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MealRequestDto(
        @NotBlank String title,
        String description,
        String imageUrl,
        String videoUrl,
        @NotNull Integer duration,
        String complexity,
        String affordability,
        @NotEmpty List<String> categories,
        @NotEmpty List<String> ingredients,
        @NotEmpty List<String> steps,
        boolean glutenFree,
        boolean lactoseFree,
        boolean vegan,
        boolean vegetarian
) {
}