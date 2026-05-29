package com.innitsocial.abcdish.controller;

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
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable String id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }
}