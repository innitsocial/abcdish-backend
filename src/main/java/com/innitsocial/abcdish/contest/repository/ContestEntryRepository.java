package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.ContestEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {
    List<ContestEntry> findByContestIdAndApprovedTrue(Long contestId);
}
