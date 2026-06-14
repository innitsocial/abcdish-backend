package com.innitsocial.abcdish.content.service;

import com.innitsocial.abcdish.content.dto.MealRequestDto;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.moderation.ContentModerationService;
import com.innitsocial.abcdish.moderation.ModerationResult;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MealService {

    private final MealRepository mealRepository;
    private final ContentModerationService contentModerationService;
    private final CatalogCapacityService catalogCapacityService;

    @Transactional(readOnly = true)
    public List<Meal> findAll() {
        return mealRepository.findAll()
                .stream()
                .map(this::initializeDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Meal> findApproved() {
        return mealRepository.findByModerationStatus(ModerationStatus.APPROVED)
                .stream()
                .map(this::initializeDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .map(this::initializeDetails)
                .orElseThrow(() -> new RuntimeException("Meal not found: " + id));
    }

    public Meal create(MealRequestDto request) {
        ModerationResult moderation = moderate(request);
        if (moderation.status() == ModerationStatus.APPROVED) {
            catalogCapacityService.ensureCapacityAvailable();
        }

        Meal meal = Meal.builder()
                .title(request.title())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .videoUrl(request.videoUrl())
                .trailerUrl(request.trailerUrl())
                .trailerType(request.trailerType())
                .promoTrailerTitle(request.promoTrailerTitle())
                .promoTrailerSubtitle(request.promoTrailerSubtitle())
                .duration(request.duration())
                .complexity(request.complexity())
                .affordability(request.affordability())
                .categories(request.categories())
                .ingredients(request.ingredients())
                .steps(request.steps())
                .glutenFree(request.glutenFree())
                .lactoseFree(request.lactoseFree())
                .vegan(request.vegan())
                .vegetarian(request.vegetarian())
                .moderationStatus(moderation.status())
                .moderationReason(moderation.reason())
                .build();

        Meal savedMeal = mealRepository.save(meal);
        ensureRecipeCode(savedMeal);
        return mealRepository.save(savedMeal);
    }

    public Meal update(Long id, MealRequestDto request) {
        Meal meal = findById(id);
        ensureRecipeCode(meal);

        meal.setTitle(request.title());
        meal.setDescription(request.description());
        meal.setImageUrl(request.imageUrl());
        meal.setVideoUrl(request.videoUrl());
        meal.setTrailerUrl(request.trailerUrl());
        meal.setTrailerType(request.trailerType());
        meal.setPromoTrailerTitle(request.promoTrailerTitle());
        meal.setPromoTrailerSubtitle(request.promoTrailerSubtitle());
        meal.setDuration(request.duration());
        meal.setComplexity(request.complexity());
        meal.setAffordability(request.affordability());
        meal.setCategories(request.categories());
        meal.setIngredients(request.ingredients());
        meal.setSteps(request.steps());
        meal.setGlutenFree(request.glutenFree());
        meal.setLactoseFree(request.lactoseFree());
        meal.setVegan(request.vegan());
        meal.setVegetarian(request.vegetarian());

        ModerationResult moderation = moderate(request);
        meal.setModerationStatus(moderation.status());
        meal.setModerationReason(moderation.reason());

        return mealRepository.save(meal);
    }

    public void delete(Long id) {
        Meal meal = findById(id);
        mealRepository.delete(meal);
    }

    private ModerationResult moderate(MealRequestDto request) {
        return contentModerationService.moderateFoodPost(List.of(
                clean(request.title()),
                clean(request.description()),
                String.join(" ", request.categories() == null ? List.of() : request.categories()),
                String.join(" ", request.ingredients() == null ? List.of() : request.ingredients()),
                String.join(" ", request.steps() == null ? List.of() : request.steps())
        ));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Meal initializeDetails(Meal meal) {
        meal.getCategories().size();
        meal.getIngredients().size();
        meal.getSteps().size();
        return meal;
    }

    private void ensureRecipeCode(Meal meal) {
        if (meal.getRecipeCode() != null && !meal.getRecipeCode().isBlank()) {
            return;
        }

        Long id = meal.getId();
        if (id == null) {
            return;
        }

        long candidate = 10000 + id;
        while (mealRepository.findByRecipeCode(String.valueOf(candidate)).isPresent()) {
            candidate++;
        }

        meal.setRecipeCode(String.valueOf(candidate));
    }
}
