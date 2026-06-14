package com.innitsocial.abcdish.content.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innitsocial.abcdish.common.cache.AppCacheService;
import com.innitsocial.abcdish.content.dto.MealRequestDto;
import com.innitsocial.abcdish.content.dto.MealResponseDto;
import com.innitsocial.abcdish.content.dto.RecipeDraftRequest;
import com.innitsocial.abcdish.content.dto.RecipeDraftResponse;
import com.innitsocial.abcdish.content.service.MealTranslationService;
import com.innitsocial.abcdish.content.service.MealService;
import com.innitsocial.abcdish.content.service.RecipeDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final RecipeDraftService recipeDraftService;
    private final MealTranslationService mealTranslationService;
    private final AppCacheService appCacheService;

    @Value("${app.cache.ttl.meal-detail-seconds:600}")
    private long mealDetailTtlSeconds;

    @DeleteMapping("/{id}")
    public void deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
        evictMealCaches(id);
    }

    @GetMapping
    public List<MealResponseDto> getAllMeals(
            @RequestHeader(value = "X-ABCDish-Language", required = false) String languageCode
    ) {
        return mealService.findApproved()
                .stream()
                .map(meal -> MealResponseDto.fromEntity(
                        meal,
                        mealTranslationService.translationFor(meal, languageCode)
                ))
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
    public MealResponseDto getMealById(
            @PathVariable Long id,
            @RequestHeader(value = "X-ABCDish-Language", required = false) String languageCode
    ) {
        String language = cleanLanguage(languageCode);
        String cacheKey = "meal-detail:v1:lang=%s:id=%d".formatted(language, id);
        return appCacheService
                .get(cacheKey, new TypeReference<MealResponseDto>() {
                })
                .orElseGet(() -> {
                    var meal = mealService.findById(id);
                    MealResponseDto response = MealResponseDto.fromEntity(
                            meal,
                            mealTranslationService.translationFor(meal, language)
                    );
                    appCacheService.set(cacheKey, response, Duration.ofSeconds(mealDetailTtlSeconds));
                    return response;
                });
    }

    @PostMapping
    public MealResponseDto createMeal(@Valid @RequestBody MealRequestDto request) {
        var response = MealResponseDto.fromEntity(mealService.create(request));
        appCacheService.evictPrefix("feed:");
        return response;
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
        var response = MealResponseDto.fromEntity(mealService.update(id, request));
        evictMealCaches(id);
        return response;
    }

    private void evictMealCaches(Long id) {
        appCacheService.evictPrefix("feed:");
        appCacheService.evictPrefix("meal-detail:v1:");
        appCacheService.evictPrefix("categories:");
    }

    private String cleanLanguage(String value) {
        String cleaned = value == null ? "" : value.trim().toLowerCase();
        if (cleaned.isBlank()) {
            return "en";
        }
        String normalized = cleaned.split("[_-]")[0].replaceAll("[^a-z0-9]", "");
        return normalized.isBlank() ? "en" : normalized;
    }
}
