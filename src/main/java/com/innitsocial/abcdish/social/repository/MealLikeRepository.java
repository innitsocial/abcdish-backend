package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MealLikeRepository extends JpaRepository<MealLike, Long> {

    long countByMealId(Long mealId);

    boolean existsByMealIdAndUserId(Long mealId, Long userId);

    Optional<MealLike> findByMealIdAndUserId(Long mealId, Long userId);

    @Query("select like.mealId, count(like.id) from MealLike like where like.mealId in :mealIds group by like.mealId")
    List<Object[]> countByMealIds(@Param("mealIds") Collection<Long> mealIds);

    @Query("select like.mealId from MealLike like where like.userId = :userId and like.mealId in :mealIds")
    List<Long> findLikedMealIds(@Param("userId") Long userId, @Param("mealIds") Collection<Long> mealIds);

    void deleteByUserId(Long userId);
}
