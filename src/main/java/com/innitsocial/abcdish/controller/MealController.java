package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.dto.MealRequestDto;
import com.innitsocial.abcdish.entity.Meal;
import com.innitsocial.abcdish.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MealController {

    private final MealService mealService;

    @GetMapping
    public List<Meal> getAllMeals() {
        return mealService.findAll();
    }

    @GetMapping("/{id}")
    public Meal getMealById(@PathVariable Long id) {
        return mealService.findById(id);
    }

    @PostMapping
    public Meal createMeal(@Valid @RequestBody MealRequestDto request) {
        return mealService.create(request);
    }

    @PutMapping("/{id}")
    public Meal updateMeal(
            @PathVariable Long id,
            @Valid @RequestBody MealRequestDto request
    ) {
        return mealService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
    }
}