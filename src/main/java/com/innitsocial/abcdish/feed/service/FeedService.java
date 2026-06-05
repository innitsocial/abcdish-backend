package com.innitsocial.abcdish.feed.service;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.feed.dto.FeedItemResponse;
import com.innitsocial.abcdish.social.repository.CreatorFollowRepository;
import com.innitsocial.abcdish.social.repository.MealCommentRepository;
import com.innitsocial.abcdish.social.repository.MealLikeRepository;
import com.innitsocial.abcdish.social.repository.MealShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final MealRepository mealRepository;
    private final MealLikeRepository mealLikeRepository;
    private final MealCommentRepository mealCommentRepository;
    private final MealShareRepository mealShareRepository;
    private final CreatorFollowRepository creatorFollowRepository;

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getHomeFeed() {
        Optional<Long> currentUserId = SecurityUtils.currentUserIdOptional();

        return mealRepository.findAll()
                .stream()
                .map(meal -> toFeedItem(meal, currentUserId))
                .toList();
    }

    private FeedItemResponse toFeedItem(Meal meal, Optional<Long> currentUserId) {
        Long mealId = meal.getId();
        String creatorKey = FeedItemResponse.creatorKey(meal);

        boolean liked = currentUserId
                .map(userId -> mealLikeRepository.existsByMealIdAndUserId(mealId, userId))
                .orElse(false);

        boolean followed = currentUserId
                .map(userId -> creatorFollowRepository.existsByCreatorKeyAndUserId(creatorKey, userId))
                .orElse(false);

        return FeedItemResponse.fromMeal(
                meal,
                mealLikeRepository.countByMealId(mealId),
                mealCommentRepository.countByMealId(mealId),
                mealShareRepository.countByMealId(mealId),
                liked,
                followed
        );
    }
}
