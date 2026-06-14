package com.innitsocial.abcdish.feed.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innitsocial.abcdish.common.cache.AppCacheService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private final AppCacheService appCacheService;

    @Value("${app.contests.acceptance-like-threshold:500}")
    private long acceptanceLikeThreshold;

    @Value("${app.cache.ttl.feed-seconds:45}")
    private long feedTtlSeconds;

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getHomeFeed(String languageCode, int page, int size) {
        Optional<Long> currentUserId = SecurityUtils.currentUserIdOptional();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        String language = cleanLanguage(languageCode);
        String cacheKey = "feed:v2:lang=%s:page=%d:size=%d".formatted(language, safePage, safeSize);

        if (currentUserId.isEmpty()) {
            Optional<List<FeedItemResponse>> cached = appCacheService.get(
                    cacheKey,
                    new TypeReference<List<FeedItemResponse>>() {
                    }
            );
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        List<FeedItemResponse> items = buildHomeFeed(language, currentUserId, safePage, safeSize);
        if (currentUserId.isEmpty()) {
            appCacheService.set(cacheKey, items, Duration.ofSeconds(feedTtlSeconds));
        }
        return items;
    }

    private List<FeedItemResponse> buildHomeFeed(
            String languageCode,
            Optional<Long> currentUserId,
            int safePage,
            int safeSize
    ) {
        int recipeLimit = Math.max(1, (int) Math.ceil(safeSize * 0.8));
        int contestLimit = Math.max(0, safeSize - recipeLimit);

        List<Meal> meals = mealRepository.findByModerationStatusOrderByIdAsc(
                ModerationStatus.APPROVED,
                PageRequest.of(safePage, recipeLimit)
        );
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        Map<Long, Long> likeCounts = mealIds.isEmpty()
                ? Map.of()
                : countMap(mealLikeRepository.countByMealIds(mealIds));
        Map<Long, Long> commentCounts = mealIds.isEmpty()
                ? Map.of()
                : countMap(mealCommentRepository.countByMealIds(mealIds));
        Map<Long, Long> shareCounts = mealIds.isEmpty()
                ? Map.of()
                : countMap(mealShareRepository.countByMealIds(mealIds));
        Set<Long> likedMealIds = currentUserId
                .map(userId -> mealIds.isEmpty()
                        ? new HashSet<Long>()
                        : new HashSet<>(mealLikeRepository.findLikedMealIds(userId, mealIds)))
                .orElseGet(HashSet::new);
        Set<String> followedCreatorKeys = currentUserId
                .map(userId -> followedCreatorKeys(userId, meals))
                .orElseGet(HashSet::new);

        List<FeedItemResponse> items = new ArrayList<>();

        items.addAll(meals.stream()
                .map(meal -> toFeedItem(
                        meal,
                        languageCode,
                        likeCounts.getOrDefault(meal.getId(), 0L),
                        commentCounts.getOrDefault(meal.getId(), 0L),
                        shareCounts.getOrDefault(meal.getId(), 0L),
                        likedMealIds.contains(meal.getId()),
                        followedCreatorKeys.contains(FeedItemResponse.creatorKey(meal))
                ))
                .toList());

        if (contestLimit > 0) {
            items.addAll(contestEntryRepository
                    .findActiveContestEntries(
                            ModerationStatus.APPROVED,
                            LocalDateTime.now(),
                            PageRequest.of(0, contestLimit)
                    )
                    .stream()
                    .map(entry -> toContestFeedItem(entry, currentUserId))
                    .toList());
        }

        return items;
    }

    public void evictFeedCache() {
        appCacheService.evictPrefix("feed:");
    }

    private FeedItemResponse toFeedItem(
            Meal meal,
            String languageCode,
            long likeCount,
            long commentCount,
            long shareCount,
            boolean liked,
            boolean followed
    ) {
        return FeedItemResponse.compactFromMeal(
                meal,
                mealTranslationService.cachedTranslationFor(meal, languageCode),
                likeCount,
                commentCount,
                shareCount,
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

    private Map<Long, Long> countMap(List<Object[]> rows) {
        Map<Long, Long> values = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length < 2 || row[0] == null || row[1] == null) continue;
            values.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return values;
    }

    private Set<String> followedCreatorKeys(Long userId, Collection<Meal> meals) {
        Set<String> creatorKeys = new HashSet<>();
        for (Meal meal : meals) {
            creatorKeys.add(FeedItemResponse.creatorKey(meal));
        }
        if (creatorKeys.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(creatorFollowRepository.findFollowedCreatorKeys(userId, creatorKeys));
    }

    private String cleanLanguage(String value) {
        String cleaned = value == null ? "" : value.trim().toLowerCase();
        if (cleaned.isBlank()) {
            return "en";
        }
        String normalized = cleaned.split("[_-]")[0].replaceAll("[^a-z0-9]", "");
        return normalized.isBlank() ? "en" : normalized;
    }
}
