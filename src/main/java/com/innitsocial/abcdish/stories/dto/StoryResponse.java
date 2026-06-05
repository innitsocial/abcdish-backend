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
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static StoryResponse fromEntity(Story story, AppUser user) {
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
                story.getCreatedAt(),
                story.getExpiresAt()
        );
    }
}
