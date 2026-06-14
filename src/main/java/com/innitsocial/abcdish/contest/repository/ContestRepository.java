package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.Contest;
import com.innitsocial.abcdish.contest.entity.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByStatus(ContestStatus status);

    @Query("""
            select contest
            from Contest contest
            where contest.status = :status
            and (contest.endsAt is null or contest.endsAt > :now)
            """)
    List<Contest> findActiveByStatus(
            @Param("status") ContestStatus status,
            @Param("now") LocalDateTime now
    );
}
