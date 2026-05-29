package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.entity.Meal;
import com.innitsocial.abcdish.service.MealService;
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
    public Meal createMeal(@RequestBody Meal meal) {
        return mealService.save(meal);
    }
}