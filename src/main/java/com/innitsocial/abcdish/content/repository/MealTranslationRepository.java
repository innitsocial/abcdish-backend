package com.innitsocial.abcdish.content.repository;

import com.innitsocial.abcdish.content.entity.MealTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealTranslationRepository extends JpaRepository<MealTranslation, Long> {
    Optional<MealTranslation> findByMealIdAndLanguageCode(Long mealId, String languageCode);
}
