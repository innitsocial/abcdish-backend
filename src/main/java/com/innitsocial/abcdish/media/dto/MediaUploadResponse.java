package com.innitsocial.abcdish.media.dto;

public record MediaUploadResponse(
        String uploadUrl,
        String publicUrl,
        String message
) {
}
