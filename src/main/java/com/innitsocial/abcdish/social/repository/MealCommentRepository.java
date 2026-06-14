package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MealCommentRepository extends JpaRepository<MealComment, Long> {

    long countByMealId(Long mealId);

    @Query("select comment.mealId, count(comment.id) from MealComment comment where comment.mealId in :mealIds group by comment.mealId")
    List<Object[]> countByMealIds(@Param("mealIds") Collection<Long> mealIds);

    List<MealComment> findTop20ByMealIdOrderByCreatedAtDesc(Long mealId);

    void deleteByUserId(Long userId);
}
