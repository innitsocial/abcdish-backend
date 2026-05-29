package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.entity.Meal;
import com.innitsocial.abcdish.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;

    public List<Meal> findAll() {
        return mealRepository.findAll();
    }

    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found: " + id));
    }

    public Meal save(Meal meal) {
        return mealRepository.save(meal);
    }
}