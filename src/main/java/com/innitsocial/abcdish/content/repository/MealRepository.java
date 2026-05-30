package com.innitsocial.abcdish.content.repository;

import com.innitsocial.abcdish.content.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
}