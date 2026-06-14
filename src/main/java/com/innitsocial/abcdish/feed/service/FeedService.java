package com.innitsocial.abcdish.feed.service;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.content.service.MealTranslationService;
import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.contest.repository.ContestEntryLikeRepository;
import com.innitsocial.abcdish.contest.repository.ContestEntryRepository;
import com.innitsocial.abcdish.feed.dto.FeedItemResponse;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import com.innitsocial.abcdish.social.repository.CreatorFollowRepository;
import com.innitsocial.abcdish.social.repository.MealCommentRepository;
import com.innitsocial.abcdish.social.repository.MealLikeRepository;
import com.innitsocial.abcdish.social.repository.MealShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ContestEntryRepository contestEntryRepository;
    private final ContestEntryLikeRepository contestEntryLikeRepository;
    private final MealTranslationService mealTranslationService;

    @Value("${app.contests.acceptance-like-threshold:500}")
    private long acceptanceLikeThreshold;

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getHomeFeed(String languageCode) {
        Optional<Long> currentUserId = SecurityUtils.currentUserIdOptional();

        List<FeedItemResponse> items = new ArrayList<>();

        items.addAll(mealRepository.findByModerationStatus(ModerationStatus.APPROVED)
                .stream()
                .map(meal -> toFeedItem(meal, currentUserId, languageCode))
                .toList());

        items.addAll(contestEntryRepository
                .findActiveContestEntries(ModerationStatus.APPROVED, LocalDateTime.now())
                .stream()
                .map(entry -> toContestFeedItem(entry, currentUserId))
                .toList());

        return items;
    }

    private FeedItemResponse toFeedItem(Meal meal, Optional<Long> currentUserId, String languageCode) {
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
                mealTranslationService.translationFor(meal, languageCode),
                mealLikeRepository.countByMealId(mealId),
                mealCommentRepository.countByMealId(mealId),
                mealShareRepository.countByMealId(mealId),
                liked,
                followed
        );
    }

    private FeedItemResponse toContestFeedItem(
            ContestEntry entry,
            Optional<Long> currentUserId
    ) {
        Long entryId = entry.getId();
        long voteCount = contestEntryLikeRepository.countByEntryId(entryId);
        boolean voted = currentUserId
                .map(userId -> contestEntryLikeRepository.existsByEntryIdAndUserId(entryId, userId))
                .orElse(false);

        return FeedItemResponse.fromContestEntry(
                entry,
                voteCount,
                voted,
                acceptanceLikeThreshold
        );
    }
}
