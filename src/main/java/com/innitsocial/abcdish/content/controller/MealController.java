package com.innitsocial.abcdish.content.controller;

import com.innitsocial.abcdish.content.dto.MealRequestDto;
import com.innitsocial.abcdish.content.dto.MealResponseDto;
import com.innitsocial.abcdish.content.dto.RecipeDraftRequest;
import com.innitsocial.abcdish.content.dto.RecipeDraftResponse;
import com.innitsocial.abcdish.content.service.MealService;
import com.innitsocial.abcdish.content.service.RecipeDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final RecipeDraftService recipeDraftService;

    @DeleteMapping("/{id}")
    public void deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
    }

    @GetMapping
    public List<MealResponseDto> getAllMeals() {
        return mealService.findApproved()
                .stream()
                .map(MealResponseDto::fromEntity)
                .toList();
    }

    @GetMapping("/manage")
    public List<MealResponseDto> getManageMeals() {
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

    @PostMapping("/draft")
    public RecipeDraftResponse createDraft(
            @RequestHeader(value = "X-ABCDish-Language", required = false) String languageCode,
            @RequestBody RecipeDraftRequest request
    ) {
        RecipeDraftRequest localizedRequest = new RecipeDraftRequest(
                request.sourceType(),
                request.sourceUrl(),
                request.titleHint(),
                request.transcript(),
                request.creatorName(),
                languageCode == null || languageCode.isBlank() ? request.languageCode() : languageCode
        );

        return recipeDraftService.createDraft(localizedRequest);
    }

    @PutMapping("/{id}")
    public MealResponseDto updateMeal(
            @PathVariable Long id,
            @Valid @RequestBody MealRequestDto request
    ) {
        return MealResponseDto.fromEntity(mealService.update(id, request));
    }
}
