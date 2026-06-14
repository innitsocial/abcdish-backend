package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.ContestEntryLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestEntryLikeRepository extends JpaRepository<ContestEntryLike, Long> {
    boolean existsByEntryIdAndUserId(Long entryId, Long userId);

    long countByEntryId(Long entryId);

    void deleteByEntryIdAndUserId(Long entryId, Long userId);

    void deleteByUserId(Long userId);
}
