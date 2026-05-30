package com.innitsocial.abcdish.content.service;

import com.innitsocial.abcdish.content.entity.Category;
import com.innitsocial.abcdish.content.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }
}