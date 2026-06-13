package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {
    List<ContestEntry> findByContestIdAndApprovedTrue(Long contestId);

    List<ContestEntry> findByContestIdAndModerationStatus(Long contestId, ModerationStatus moderationStatus);

    void deleteByUserId(Long userId);
}
