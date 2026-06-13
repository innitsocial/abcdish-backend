package com.innitsocial.abcdish.content.repository;

import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByModerationStatus(ModerationStatus moderationStatus);
}
