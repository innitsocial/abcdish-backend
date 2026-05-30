package com.innitsocial.abcdish.contest.repository;

import com.innitsocial.abcdish.contest.entity.Contest;
import com.innitsocial.abcdish.contest.entity.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByStatus(ContestStatus status);
}
