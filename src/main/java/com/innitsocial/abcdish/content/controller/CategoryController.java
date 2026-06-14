package com.innitsocial.abcdish.content.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innitsocial.abcdish.common.cache.AppCacheService;
import com.innitsocial.abcdish.content.dto.CategoryResponseDto;
import com.innitsocial.abcdish.content.entity.Category;
import com.innitsocial.abcdish.content.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final String CATEGORIES_CACHE_KEY = "categories:v1";

    private final CategoryService categoryService;
    private final AppCacheService appCacheService;

    @Value("${app.cache.ttl.categories-seconds:3600}")
    private long categoriesTtlSeconds;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return appCacheService
                .get(CATEGORIES_CACHE_KEY, new TypeReference<List<CategoryResponseDto>>() {
                })
                .orElseGet(() -> {
                    List<CategoryResponseDto> categories = categoryService.findAll()
                            .stream()
                            .map(CategoryResponseDto::fromEntity)
                            .toList();
                    appCacheService.set(CATEGORIES_CACHE_KEY, categories, Duration.ofSeconds(categoriesTtlSeconds));
                    return categories;
                });
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getCategoryById(@PathVariable String id) {
        return getAllCategories()
                .stream()
                .filter(category -> category.id().equals(id))
                .findFirst()
                .orElseGet(() -> CategoryResponseDto.fromEntity(categoryService.findById(id)));
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        Category saved = categoryService.save(category);
        appCacheService.evictPrefix("categories:");
        appCacheService.evictPrefix("feed:");
        return saved;
    }
}
