package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealShareRepository extends JpaRepository<MealShare, Long> {

    long countByMealId(Long mealId);
}
