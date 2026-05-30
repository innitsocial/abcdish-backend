package com.innitsocial.abcdish.content.dto;

import com.innitsocial.abcdish.content.entity.Category;

public record CategoryResponseDto(
        String id,
        String title,
        String colorCode
) {
    public static CategoryResponseDto fromEntity(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getTitle(),
                category.getColorCode()
        );
    }
}