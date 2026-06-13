package com.innitsocial.abcdish.safety.dto;

import jakarta.validation.constraints.NotBlank;

public record ContentReportRequest(
        @NotBlank String targetType,
        @NotBlank String targetId,
        @NotBlank String reason,
        String details
) {
}
