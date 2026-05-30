package com.innitsocial.abcdish.shopping.dto;

public record ShoppingListResponse(
        Long id,
        String ingredientName,
        String quantity,
        boolean purchased
) {
}