package com.innitsocial.abcdish.contest.dto;

import com.innitsocial.abcdish.contest.entity.Contest;

import java.time.LocalDateTime;

public record ContestResponse(
        Long id,
        String title,
        String description,
        String prizeDescription,
        String status,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
    public static ContestResponse fromEntity(Contest contest) {
        return new ContestResponse(
                contest.getId(),
                contest.getTitle(),
                contest.getDescription(),
                contest.getPrizeDescription(),
                contest.getStatus().name(),
                contest.getStartsAt(),
                contest.getEndsAt()
        );
    }
}
