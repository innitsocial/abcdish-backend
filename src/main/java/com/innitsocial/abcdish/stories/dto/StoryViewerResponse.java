package com.innitsocial.abcdish.stories.dto;

import java.time.LocalDateTime;

public record StoryViewerResponse(
        Long userId,
        String name,
        LocalDateTime viewedAt
) {
}
