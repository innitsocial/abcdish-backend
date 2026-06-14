package com.innitsocial.abcdish.stories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoryRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 2000)
        String caption,

        @Size(max = 1000)
        String imageUrl,

        @Size(max = 1000)
        String videoUrl,

        Long contestEntryId
) {
}
