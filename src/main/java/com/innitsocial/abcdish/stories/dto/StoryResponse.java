package com.innitsocial.abcdish.stories.dto;

import com.innitsocial.abcdish.auth.entity.AppUser;
import com.innitsocial.abcdish.stories.entity.Story;

import java.time.LocalDateTime;

public record StoryResponse(
        Long id,
        Long userId,
        String title,
        String caption,
        String creatorName,
        String imageUrl,
        String videoUrl,
        Long contestEntryId,
        String promotedVideoTitle,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        String moderationStatus,
        String moderationReason,
        long viewCount,
        long likeCount,
        boolean likedByCurrentUser
) {
    public static StoryResponse fromEntity(Story story, AppUser user) {
        return fromEntity(story, user, 0, 0, false);
    }

    public static StoryResponse fromEntity(
            Story story,
            AppUser user,
            long viewCount,
            long likeCount,
            boolean likedByCurrentUser
    ) {
        String creatorName = user.getName() == null || user.getName().isBlank()
                ? "ABCDish Creator"
                : user.getName();

        return new StoryResponse(
                story.getId(),
                story.getUserId(),
                story.getTitle(),
                story.getCaption(),
                creatorName,
                story.getImageUrl(),
                story.getVideoUrl(),
                story.getContestEntryId(),
                story.getPromotedVideoTitle(),
                story.getCreatedAt(),
                story.getExpiresAt(),
                story.getModerationStatus() == null ? "PENDING_REVIEW" : story.getModerationStatus().name(),
                story.getModerationReason(),
                viewCount,
                likeCount,
                likedByCurrentUser
        );
    }
}
