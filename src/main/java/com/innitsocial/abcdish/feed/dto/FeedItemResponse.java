package com.innitsocial.abcdish.feed.dto;

import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.entity.MealTranslation;
import com.innitsocial.abcdish.contest.entity.ContestEntry;

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
        boolean followedByCurrentUser,
        Long contestId,
        long acceptanceThreshold,
        Long acceptedMealId,
        boolean reviewUnlocked,
        String competitionCategory,
        String competitionStatus,
        Integer finalistRank,
        boolean londonQualified,
        Integer prizeAmountGbp
) {
    public static FeedItemResponse fromMeal(
            Meal meal,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean likedByCurrentUser,
            boolean followedByCurrentUser
    ) {
        return fromMeal(meal, null, likeCount, commentCount, shareCount, likedByCurrentUser, followedByCurrentUser);
    }

    public static FeedItemResponse compactFromMeal(
            Meal meal,
            MealTranslation translation,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean likedByCurrentUser,
            boolean followedByCurrentUser
    ) {
        return fromMeal(
                meal,
                translation,
                List.of(),
                List.of(),
                likeCount,
                commentCount,
                shareCount,
                likedByCurrentUser,
                followedByCurrentUser
        );
    }

    public static FeedItemResponse fromMeal(
            Meal meal,
            MealTranslation translation,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean likedByCurrentUser,
            boolean followedByCurrentUser
    ) {
        String creatorKey = creatorKey(meal);
        String title = translation == null ? meal.getTitle() : translation.getTitle();
        String description = translation == null ? meal.getDescription() : translation.getDescription();
        List<String> ingredients = translation == null ? meal.getIngredients() : translation.getIngredients();
        List<String> steps = translation == null ? meal.getSteps() : translation.getSteps();

        return fromMeal(
                meal,
                translation,
                ingredients,
                steps,
                likeCount,
                commentCount,
                shareCount,
                likedByCurrentUser,
                followedByCurrentUser
        );
    }

    private static FeedItemResponse fromMeal(
            Meal meal,
            MealTranslation translation,
            List<String> ingredients,
            List<String> steps,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean likedByCurrentUser,
            boolean followedByCurrentUser
    ) {
        String creatorKey = creatorKey(meal);
        String title = translation == null ? meal.getTitle() : translation.getTitle();
        String description = translation == null ? meal.getDescription() : translation.getDescription();

        return new FeedItemResponse(
                meal.getId(),
                title,
                description,
                meal.getImageUrl(),
                meal.getVideoUrl(),
                meal.getDuration(),
                meal.getComplexity(),
                meal.getAffordability(),
                meal.getCategories(),
                ingredients,
                steps,
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
                followedByCurrentUser,
                null,
                0,
                null,
                false,
                "admin",
                "OFFICIAL_RECIPE",
                null,
                false,
                null
        );
    }

    public static FeedItemResponse fromContestEntry(
            ContestEntry entry,
            long voteCount,
            boolean likedByCurrentUser,
            long acceptanceThreshold
    ) {
        return new FeedItemResponse(
                entry.getId(),
                entry.getTitle(),
                entry.getDescription(),
                entry.getThumbnailUrl(),
                entry.getVideoUrl(),
                entry.getDuration() == null ? 30 : entry.getDuration(),
                entry.getComplexity() == null || entry.getComplexity().isBlank()
                        ? "simple"
                        : entry.getComplexity(),
                "affordable",
                List.of("contest"),
                List.of(),
                List.of(),
                entry.isGlutenFree(),
                entry.isLactoseFree(),
                entry.isVegan(),
                entry.isVegetarian(),
                "CONTEST_ENTRY",
                "abcdish-contest",
                "ABCDish Challenge",
                voteCount,
                0,
                0,
                likedByCurrentUser,
                false,
                entry.getContestId(),
                acceptanceThreshold,
                entry.getAcceptedMealId(),
                voteCount >= acceptanceThreshold,
                entry.getCompetitionCategory(),
                entry.getCompetitionStatus(),
                entry.getFinalistRank(),
                entry.isLondonQualified(),
                entry.getPrizeAmountGbp()
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
