package com.innitsocial.abcdish.membership.repository;

import com.innitsocial.abcdish.membership.entity.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    long countByUserIdAndViewMonth(Long userId, LocalDate viewMonth);

    Optional<VideoView> findByUserIdAndMealIdAndViewMonth(
            Long userId,
            Long mealId,
            LocalDate viewMonth
    );
}