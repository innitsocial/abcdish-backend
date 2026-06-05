package com.innitsocial.abcdish.social.service;

import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.social.dto.CommentRequest;
import com.innitsocial.abcdish.social.dto.CommentResponse;
import com.innitsocial.abcdish.social.dto.SocialActionResponse;
import com.innitsocial.abcdish.social.entity.CreatorFollow;
import com.innitsocial.abcdish.social.entity.MealComment;
import com.innitsocial.abcdish.social.entity.MealLike;
import com.innitsocial.abcdish.social.entity.MealShare;
import com.innitsocial.abcdish.social.repository.CreatorFollowRepository;
import com.innitsocial.abcdish.social.repository.MealCommentRepository;
import com.innitsocial.abcdish.social.repository.MealLikeRepository;
import com.innitsocial.abcdish.social.repository.MealShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialService {

    private final MealRepository mealRepository;
    private final MealLikeRepository mealLikeRepository;
    private final MealCommentRepository mealCommentRepository;
    private final MealShareRepository mealShareRepository;
    private final CreatorFollowRepository creatorFollowRepository;

    public SocialActionResponse likeMeal(Long mealId, Long userId) {
        ensureMealExists(mealId);

        if (!mealLikeRepository.existsByMealIdAndUserId(mealId, userId)) {
            mealLikeRepository.save(MealLike.builder()
                    .mealId(mealId)
                    .userId(userId)
                    .build());
        }

        return new SocialActionResponse(true, mealLikeRepository.countByMealId(mealId));
    }

    public SocialActionResponse unlikeMeal(Long mealId, Long userId) {
        mealLikeRepository.findByMealIdAndUserId(mealId, userId)
                .ifPresent(mealLikeRepository::delete);

        return new SocialActionResponse(false, mealLikeRepository.countByMealId(mealId));
    }

    public SocialActionResponse recordShare(Long mealId, Long userId) {
        ensureMealExists(mealId);

        mealShareRepository.save(MealShare.builder()
                .mealId(mealId)
                .userId(userId)
                .build());

        return new SocialActionResponse(true, mealShareRepository.countByMealId(mealId));
    }

    public SocialActionResponse followCreator(String creatorKey, Long userId) {
        String cleanedCreatorKey = normalizeCreatorKey(creatorKey);

        if (!creatorFollowRepository.existsByCreatorKeyAndUserId(cleanedCreatorKey, userId)) {
            creatorFollowRepository.save(CreatorFollow.builder()
                    .creatorKey(cleanedCreatorKey)
                    .userId(userId)
                    .build());
        }

        return new SocialActionResponse(true, 0);
    }

    public SocialActionResponse unfollowCreator(String creatorKey, Long userId) {
        String cleanedCreatorKey = normalizeCreatorKey(creatorKey);

        creatorFollowRepository.findByCreatorKeyAndUserId(cleanedCreatorKey, userId)
                .ifPresent(creatorFollowRepository::delete);

        return new SocialActionResponse(false, 0);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getMealComments(Long mealId) {
        ensureMealExists(mealId);

        return mealCommentRepository.findTop20ByMealIdOrderByCreatedAtDesc(mealId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    public CommentResponse addComment(Long mealId, Long userId, CommentRequest request) {
        ensureMealExists(mealId);

        MealComment comment = mealCommentRepository.save(MealComment.builder()
                .mealId(mealId)
                .userId(userId)
                .commentText(request.text().trim())
                .build());

        return CommentResponse.fromEntity(comment);
    }

    private void ensureMealExists(Long mealId) {
        if (!mealRepository.existsById(mealId)) {
            throw new RuntimeException("Meal not found: " + mealId);
        }
    }

    private String normalizeCreatorKey(String creatorKey) {
        String cleanedCreatorKey = creatorKey == null ? "" : creatorKey.trim();

        if (cleanedCreatorKey.isEmpty()) {
            throw new RuntimeException("Creator key is required");
        }

        return cleanedCreatorKey;
    }
}
