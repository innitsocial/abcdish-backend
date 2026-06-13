package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealCommentRepository extends JpaRepository<MealComment, Long> {

    long countByMealId(Long mealId);

    List<MealComment> findTop20ByMealIdOrderByCreatedAtDesc(Long mealId);

    void deleteByUserId(Long userId);
}
