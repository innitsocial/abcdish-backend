package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealLikeRepository extends JpaRepository<MealLike, Long> {

    long countByMealId(Long mealId);

    boolean existsByMealIdAndUserId(Long mealId, Long userId);

    Optional<MealLike> findByMealIdAndUserId(Long mealId, Long userId);

    void deleteByUserId(Long userId);
}
