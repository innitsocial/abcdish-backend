package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.MealShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MealShareRepository extends JpaRepository<MealShare, Long> {

    long countByMealId(Long mealId);

    @Query("select share.mealId, count(share.id) from MealShare share where share.mealId in :mealIds group by share.mealId")
    List<Object[]> countByMealIds(@Param("mealIds") Collection<Long> mealIds);

    void deleteByUserId(Long userId);
}
