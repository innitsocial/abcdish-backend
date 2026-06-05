package com.innitsocial.abcdish.social.dto;

import com.innitsocial.abcdish.social.entity.MealComment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long mealId,
        Long userId,
        String text,
        LocalDateTime createdAt
) {
    public static CommentResponse fromEntity(MealComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getMealId(),
                comment.getUserId(),
                comment.getCommentText(),
                comment.getCreatedAt()
        );
    }
}
