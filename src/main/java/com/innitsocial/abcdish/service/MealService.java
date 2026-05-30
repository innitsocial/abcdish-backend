package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.dto.MealRequestDto;
import com.innitsocial.abcdish.entity.Meal;
import com.innitsocial.abcdish.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MealService {

    private final MealRepository mealRepository;

    @Transactional(readOnly = true)
    public List<Meal> findAll() {
        return mealRepository.findAll();
    }

    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found: " + id));
    }

    public Meal create(MealRequestDto request) {
        Meal meal = Meal.builder()
                .title(request.title())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .videoUrl(request.videoUrl())
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
                .build();

        return mealRepository.save(meal);
    }

    public Meal update(Long id, MealRequestDto request) {
        Meal meal = findById(id);

        meal.setTitle(request.title());
        meal.setDescription(request.description());
        meal.setImageUrl(request.imageUrl());
        meal.setVideoUrl(request.videoUrl());
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

        return mealRepository.save(meal);
    }

    public void delete(Long id) {
        Meal meal = findById(id);
        mealRepository.delete(meal);
    }
}