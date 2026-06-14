package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.ContestEntry;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {
    List<ContestEntry> findByContestIdAndApprovedTrue(Long contestId);

    List<ContestEntry> findByContestIdAndModerationStatus(Long contestId, ModerationStatus moderationStatus);

    List<ContestEntry> findByContestIdAndEligibleForVotingTrueOrderByVotesDesc(Long contestId);

    List<ContestEntry> findByUserIdAndModerationStatusOrderByCreatedAtDesc(
            Long userId,
            ModerationStatus moderationStatus
    );

    @Query("""
            select entry
            from ContestEntry entry
            where entry.contestId = :contestId
            and entry.moderationStatus = :moderationStatus
            and entry.eligibleForVoting = true
            and (
                exists (
                    select contest.id
                    from Contest contest
                    where contest.id = entry.contestId
                    and contest.status = com.innitsocial.abcdish.contest.entity.ContestStatus.OPEN
                    and (contest.endsAt is null or contest.endsAt > :now)
                )
            )
            """)
    List<ContestEntry> findVisibleEntriesForContest(
            @Param("contestId") Long contestId,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("now") LocalDateTime now
    );

    @Query("""
            select entry
            from ContestEntry entry
            where entry.moderationStatus = :moderationStatus
            and entry.eligibleForVoting = true
            and entry.competitionStatus <> 'WINNER'
            and exists (
                select contest.id
                from Contest contest
                where contest.id = entry.contestId
                and contest.status = com.innitsocial.abcdish.contest.entity.ContestStatus.OPEN
                and (contest.endsAt is null or contest.endsAt > :now)
            )
            """)
    List<ContestEntry> findActiveContestEntries(
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("now") LocalDateTime now
    );

    @Query("""
            select entry
            from ContestEntry entry
            where entry.moderationStatus = :moderationStatus
            and entry.eligibleForVoting = true
            and entry.competitionStatus <> 'WINNER'
            and exists (
                select contest.id
                from Contest contest
                where contest.id = entry.contestId
                and contest.status = com.innitsocial.abcdish.contest.entity.ContestStatus.OPEN
                and (contest.endsAt is null or contest.endsAt > :now)
            )
            order by entry.votes desc, entry.createdAt desc
            """)
    List<ContestEntry> findActiveContestEntries(
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    void deleteByUserId(Long userId);
}
