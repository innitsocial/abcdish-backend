package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.dto.CategoryResponseDto;
import com.innitsocial.abcdish.entity.Category;
import com.innitsocial.abcdish.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.findAll()
                .stream()
                .map(CategoryResponseDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getCategoryById(@PathVariable String id) {
        return CategoryResponseDto.fromEntity(categoryService.findById(id));
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }
}