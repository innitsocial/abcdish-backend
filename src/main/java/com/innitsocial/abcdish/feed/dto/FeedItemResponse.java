package com.innitsocial.abcdish.feed.dto;

import com.innitsocial.abcdish.content.entity.Meal;

import java.util.List;

public record FeedItemResponse(
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
        boolean vegetarian,
        String sourceType,
        String creatorKey,
        String creatorName,
        long likeCount,
        long commentCount,
        long shareCount,
        boolean likedByCurrentUser,
        boolean followedByCurrentUser
) {
    public static FeedItemResponse fromMeal(
            Meal meal,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean likedByCurrentUser,
            boolean followedByCurrentUser
    ) {
        String creatorKey = creatorKey(meal);

        return new FeedItemResponse(
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
                meal.isVegetarian(),
                "RECIPE",
                creatorKey,
                creatorName(creatorKey),
                likeCount,
                commentCount,
                shareCount,
                likedByCurrentUser,
                followedByCurrentUser
        );
    }

    public static String creatorKey(Meal meal) {
        if (meal.getCategories() == null || meal.getCategories().isEmpty()) {
            return "abcdish";
        }

        String firstCategory = meal.getCategories().getFirst();
        if (firstCategory == null || firstCategory.isBlank()) {
            return "abcdish";
        }

        return firstCategory.trim().toLowerCase();
    }

    private static String creatorName(String creatorKey) {
        String cleaned = creatorKey.replace('-', ' ').trim();
        if (cleaned.isEmpty()) {
            return "ABCDish Kitchen";
        }

        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1) + " Kitchen";
    }
}
