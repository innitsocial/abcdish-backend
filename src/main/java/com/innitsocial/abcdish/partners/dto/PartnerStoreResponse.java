package com.innitsocial.abcdish.partners.dto;

public record PartnerStoreResponse(
        Long id,
        String storeName,
        String postcode,
        String websiteUrl
) {
}