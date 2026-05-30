package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.dto.MealRequestDto;
import com.innitsocial.abcdish.dto.MealResponseDto;
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

    @DeleteMapping("/{id}")
    public void deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
    }

    @GetMapping
    public List<MealResponseDto> getAllMeals() {
        return mealService.findAll()
                .stream()
                .map(MealResponseDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public MealResponseDto getMealById(@PathVariable Long id) {
        return MealResponseDto.fromEntity(mealService.findById(id));
    }

    @PostMapping
    public MealResponseDto createMeal(@Valid @RequestBody MealRequestDto request) {
        return MealResponseDto.fromEntity(mealService.create(request));
    }

    @PutMapping("/{id}")
    public MealResponseDto updateMeal(
            @PathVariable Long id,
            @Valid @RequestBody MealRequestDto request
    ) {
        return MealResponseDto.fromEntity(mealService.update(id, request));
    }
}