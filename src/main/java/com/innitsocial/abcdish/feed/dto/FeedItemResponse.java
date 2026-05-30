package com.innitsocial.abcdish.feed.dto;

import com.innitsocial.abcdish.content.entity.Meal;

public record FeedItemResponse(
        Long mealId,
        String title,
        String description,
        String imageUrl,
        String videoUrl,
        Integer duration,
        String sourceType
) {
    public static FeedItemResponse fromMeal(Meal meal) {
        return new FeedItemResponse(
                meal.getId(),
                meal.getTitle(),
                meal.getDescription(),
                meal.getImageUrl(),
                meal.getVideoUrl(),
                meal.getDuration(),
                "RECIPE"
        );
    }
}
