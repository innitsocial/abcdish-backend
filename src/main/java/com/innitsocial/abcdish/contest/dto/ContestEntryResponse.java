package com.innitsocial.abcdish.contest.dto;

import com.innitsocial.abcdish.contest.entity.ContestEntry;

public record ContestEntryResponse(
        Long id,
        Long contestId,
        Long userId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        long votes,
        boolean approved,
        boolean likedByCurrentUser,
        long acceptanceThreshold,
        Long acceptedMealId,
        String moderationStatus,
        String moderationReason
) {
    public static ContestEntryResponse fromEntity(
            ContestEntry entry,
            boolean likedByCurrentUser,
            long acceptanceThreshold
    ) {
        return new ContestEntryResponse(
                entry.getId(),
                entry.getContestId(),
                entry.getUserId(),
                entry.getTitle(),
                entry.getDescription(),
                entry.getVideoUrl(),
                entry.getThumbnailUrl(),
                entry.getVotes(),
                entry.isApproved(),
                likedByCurrentUser,
                acceptanceThreshold,
                entry.getAcceptedMealId(),
                entry.getModerationStatus() == null ? "PENDING_REVIEW" : entry.getModerationStatus().name(),
                entry.getModerationReason()
        );
    }
}
