package com.innitsocial.abcdish.shopping.dto;

public record ShoppingCheckoutOptionResponse(
        Long partnerStoreId,
        String storeName,
        String postcode,
        String websiteUrl,
        int itemCount,
        String message
) {
}
