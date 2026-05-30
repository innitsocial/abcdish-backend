package com.innitsocial.abcdish.shopping.dto;

public record ShoppingListRequest(
        String ingredientName,
        String quantity
) {
}