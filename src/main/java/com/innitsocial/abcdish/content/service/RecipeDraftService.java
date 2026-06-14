package com.innitsocial.abcdish.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innitsocial.abcdish.auth.repository.AppUserRepository;
import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.content.dto.RecipeDraftRequest;
import com.innitsocial.abcdish.content.dto.RecipeDraftResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDraftService {

    private final ObjectMapper objectMapper;
    private final AppUserRepository appUserRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.recipe-extraction.enabled:true}")
    private boolean aiExtractionEnabled;

    @Value("${app.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4.1-mini}")
    private String openAiModel;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    public RecipeDraftResponse createDraft(RecipeDraftRequest request) {
        String sourceUrl = clean(request.sourceUrl());
        String sourceType = clean(request.sourceType()).isBlank()
                ? "OWN_VIDEO"
                : clean(request.sourceType()).toUpperCase();
        if (!"OWN_VIDEO".equals(sourceType)) {
            throw new IllegalArgumentException("ABCDish only supports managed uploaded recipe videos.");
        }
        String title = titleFrom(request.titleHint(), sourceUrl, sourceType);
        String creatorName = creatorNameFrom(request);
        String languageName = languageName(request.languageCode());
        VideoMetadata metadata = metadataFor(sourceType, sourceUrl);

        if (aiExtractionEnabled && !clean(openAiApiKey).isBlank()) {
            try {
                return extractWithOpenAi(request, sourceType, sourceUrl, title, creatorName, languageName, metadata);
            } catch (Exception error) {
                log.warn("AI recipe extraction failed for sourceType={} sourceUrl={}: {}",
                        sourceType, sourceUrl, error.getMessage());
            }
        }

        return fallbackDraft(sourceType, sourceUrl, title, creatorName, metadata,
                clean(openAiApiKey).isBlank()
                        ? "AI extraction is not configured. Add OPENAI_API_KEY to Railway to enable it."
                        : "AI extraction failed, so ABCDish created a verification draft.");
    }

    private RecipeDraftResponse extractWithOpenAi(
            RecipeDraftRequest request,
            String sourceType,
            String sourceUrl,
            String fallbackTitle,
            String creatorName,
            String languageName,
            VideoMetadata metadata
    ) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", openAiModel);
        body.put("input", List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                You extract cooking recipe data for ABCDish, a food social app.
                                Return only food-related recipe details. If source evidence is thin,
                                produce a conservative draft and make the user verify it.
                                Do not invent health claims. Keep ingredients and steps practical.
                                ABCDish uses managed, uploaded videos only. Do not rely on external video platforms.
                                Return all user-facing recipe text in the requested language.
                                """
                ),
                Map.of(
                        "role", "user",
                        "content", extractionPrompt(request, sourceType, sourceUrl, fallbackTitle, creatorName, languageName, metadata)
                )
        ));
        body.put("text", Map.of("format", structuredOutputFormat()));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openAiBaseUrl.replaceAll("/$", "") + "/responses"))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI responded with HTTP " + response.statusCode());
        }

        String outputJson = extractOutputText(response.body());
        AiRecipeDraft draft = objectMapper.readValue(outputJson, AiRecipeDraft.class);
        String trailerType = cleanOrDefault(draft.trailerType(), "VIDEO");
        String imageUrl = clean(draft.imageUrl()).isBlank() ? metadata.thumbnailUrl() : clean(draft.imageUrl());

        return new RecipeDraftResponse(
                cleanOrDefault(draft.title(), fallbackTitle),
                cleanOrDefault(draft.description(), "Please verify this AI draft before publishing."),
                imageUrl,
                sourceUrl,
                clean(draft.trailerUrl()),
                trailerType,
                cleanOrDefault(draft.promoTrailerTitle(), cleanOrDefault(draft.title(), fallbackTitle)),
                cleanOrDefault(draft.promoTrailerSubtitle(), "By " + creatorName + " on ABCDish"),
                draft.duration() == null || draft.duration() <= 0 ? 30 : draft.duration(),
                cleanOrDefault(draft.complexity(), "simple"),
                cleanOrDefault(draft.affordability(), "affordable"),
                nonEmpty(draft.categories(), List.of("cooking")),
                nonEmpty(draft.ingredients(), List.of("Verify ingredients from the source video")),
                nonEmpty(draft.steps(), List.of("Review the source video", "Verify ingredients", "Update cooking steps")),
                Boolean.TRUE.equals(draft.glutenFree()),
                Boolean.TRUE.equals(draft.lactoseFree()),
                Boolean.TRUE.equals(draft.vegan()),
                Boolean.TRUE.equals(draft.vegetarian()),
                "AI_DRAFT",
                "AI extracted a draft. Please verify details before publishing."
        );
    }

    private String extractionPrompt(
            RecipeDraftRequest request,
            String sourceType,
            String sourceUrl,
            String fallbackTitle,
            String creatorName,
            String languageName,
            VideoMetadata metadata
    ) {
        return """
                Requested output language: %s
                Source type: %s
                Source URL: %s
                Creator/user name: %s
                User title hint: %s
                Metadata title: %s
                Metadata author: %s
                Metadata thumbnail: %s
                Transcript or creator notes, if supplied:
                %s

                Create a recipe draft for the upload form.
                Write title, description, categories, ingredients, steps, promoTrailerTitle,
                and promoTrailerSubtitle in the requested output language.
                Keep enum values exactly in English: trailerType, complexity, affordability.
                If source type is OWN_VIDEO, set trailerType to VIDEO when a real trailer clip still
                needs to be uploaded or generated. Do not claim a trailer has been generated unless
                trailerUrl is present in the source notes.
                """
                .formatted(
                        languageName,
                        sourceType,
                        sourceUrl,
                        creatorName,
                        cleanOrDefault(request.titleHint(), fallbackTitle),
                        clean(metadata.title()),
                        clean(metadata.authorName()),
                        clean(metadata.thumbnailUrl()),
                        clean(request.transcript()).isBlank()
                                ? "No transcript supplied."
                                : clean(request.transcript())
                );
    }

    private Map<String, Object> structuredOutputFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of(
                "title", "description", "imageUrl", "trailerUrl", "trailerType",
                "promoTrailerTitle", "promoTrailerSubtitle", "duration", "complexity",
                "affordability", "categories", "ingredients", "steps", "glutenFree",
                "lactoseFree", "vegan", "vegetarian"
        ));
        schema.put("properties", Map.ofEntries(
                Map.entry("title", Map.of("type", "string")),
                Map.entry("description", Map.of("type", "string")),
                Map.entry("imageUrl", Map.of("type", "string")),
                Map.entry("trailerUrl", Map.of("type", "string")),
                Map.entry("trailerType", Map.of("type", "string", "enum", List.of("VIDEO", "PROMO_TEXT"))),
                Map.entry("promoTrailerTitle", Map.of("type", "string")),
                Map.entry("promoTrailerSubtitle", Map.of("type", "string")),
                Map.entry("duration", Map.of("type", "integer")),
                Map.entry("complexity", Map.of("type", "string", "enum", List.of("simple", "challenging", "hard"))),
                Map.entry("affordability", Map.of("type", "string", "enum", List.of("affordable", "pricey", "luxurious"))),
                Map.entry("categories", Map.of("type", "array", "items", Map.of("type", "string"))),
                Map.entry("ingredients", Map.of("type", "array", "items", Map.of("type", "string"))),
                Map.entry("steps", Map.of("type", "array", "items", Map.of("type", "string"))),
                Map.entry("glutenFree", Map.of("type", "boolean")),
                Map.entry("lactoseFree", Map.of("type", "boolean")),
                Map.entry("vegan", Map.of("type", "boolean")),
                Map.entry("vegetarian", Map.of("type", "boolean"))
        ));

        return Map.of(
                "type", "json_schema",
                "name", "abcdish_recipe_draft",
                "strict", true,
                "schema", schema
        );
    }

    private String extractOutputText(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode direct = root.path("output_text");
        if (direct.isTextual() && !direct.asText().isBlank()) {
            return direct.asText();
        }

        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                JsonNode text = content.path("text");
                if (text.isTextual() && !text.asText().isBlank()) {
                    return text.asText();
                }
            }
        }

        throw new JsonProcessingException("OpenAI response did not include output_text") {
        };
    }

    private VideoMetadata metadataFor(String sourceType, String sourceUrl) {
        return new VideoMetadata("", "", "");
    }

    private RecipeDraftResponse fallbackDraft(
            String sourceType,
            String sourceUrl,
            String title,
            String creatorName,
            VideoMetadata metadata,
            String message
    ) {
        String finalTitle = clean(metadata.title()).isBlank() ? title : metadata.title();
        String promoSubtitle = "By " + (clean(metadata.authorName()).isBlank() ? creatorName : metadata.authorName())
                + " · Tap to cook on ABCDish";

        return new RecipeDraftResponse(
                finalTitle,
                "Recipe draft based on " + sourceLabel(sourceType) + ". Please verify ingredients and steps before publishing.",
                clean(metadata.thumbnailUrl()),
                sourceUrl,
                "",
                "VIDEO",
                finalTitle,
                promoSubtitle,
                30,
                "simple",
                "affordable",
                List.of("cooking"),
                List.of("Add ingredients after verifying the recipe"),
                List.of("Review the video", "Update ingredients", "Write clear cooking steps"),
                false,
                false,
                false,
                false,
                "DRAFT",
                message
        );
    }

    private String creatorNameFrom(RecipeDraftRequest request) {
        String supplied = clean(request.creatorName());
        if (!supplied.isBlank()) {
            return supplied;
        }

        return SecurityUtils.currentUserIdOptional()
                .flatMap(appUserRepository::findById)
                .map(user -> clean(user.getName()))
                .filter(name -> !name.isBlank())
                .orElse("ABCDish creator");
    }

    private String titleFrom(String titleHint, String sourceUrl, String sourceType) {
        String cleanedHint = clean(titleHint);
        if (!cleanedHint.isBlank()) {
            return cleanedHint;
        }

        try {
            String path = URI.create(sourceUrl).getPath();
            if (path != null && !path.isBlank()) {
                String fileName = path.substring(path.lastIndexOf('/') + 1)
                        .replace('-', ' ')
                        .replace('_', ' ');
                int dotIndex = fileName.lastIndexOf('.');
                return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
            }
        } catch (IllegalArgumentException ignored) {
            // Keep fallback below.
        }

        return "Cooking Recipe";
    }

    private String sourceLabel(String sourceType) {
        return "the uploaded video";
    }

    private String languageName(String languageCode) {
        return switch (clean(languageCode).toLowerCase()) {
            case "am" -> "Amharic";
            case "ar" -> "Arabic";
            case "bn" -> "Bengali";
            case "cs" -> "Czech";
            case "da" -> "Danish";
            case "de" -> "German";
            case "el" -> "Greek";
            case "es" -> "Spanish";
            case "fa" -> "Persian";
            case "fi" -> "Finnish";
            case "fr" -> "French";
            case "gu" -> "Gujarati";
            case "ha" -> "Hausa";
            case "he" -> "Hebrew";
            case "hi" -> "Hindi";
            case "id" -> "Indonesian";
            case "it" -> "Italian";
            case "ja" -> "Japanese";
            case "jv" -> "Javanese";
            case "kn" -> "Kannada";
            case "ko" -> "Korean";
            case "ml" -> "Malayalam";
            case "mr" -> "Marathi";
            case "ms" -> "Malay";
            case "my" -> "Burmese";
            case "nl" -> "Dutch";
            case "no" -> "Norwegian";
            case "or" -> "Odia";
            case "pa" -> "Punjabi";
            case "pl" -> "Polish";
            case "pt" -> "Portuguese";
            case "ro" -> "Romanian";
            case "ru" -> "Russian";
            case "sv" -> "Swedish";
            case "sw" -> "Swahili";
            case "ta" -> "Tamil";
            case "te" -> "Telugu";
            case "th" -> "Thai";
            case "tl" -> "Filipino";
            case "tr" -> "Turkish";
            case "uk" -> "Ukrainian";
            case "ur" -> "Urdu";
            case "vi" -> "Vietnamese";
            case "yo" -> "Yoruba";
            case "zh" -> "Chinese";
            case "zu" -> "Zulu";
            default -> "English";
        };
    }

    private String cleanOrDefault(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> nonEmpty(List<String> value, List<String> fallback) {
        if (value == null) {
            return fallback;
        }
        List<String> cleaned = value.stream()
                .map(this::clean)
                .filter(item -> !item.isBlank())
                .toList();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private record VideoMetadata(String title, String authorName, String thumbnailUrl) {
    }

    private record AiRecipeDraft(
            String title,
            String description,
            String imageUrl,
            String trailerUrl,
            String trailerType,
            String promoTrailerTitle,
            String promoTrailerSubtitle,
            Integer duration,
            String complexity,
            String affordability,
            List<String> categories,
            List<String> ingredients,
            List<String> steps,
            Boolean glutenFree,
            Boolean lactoseFree,
            Boolean vegan,
            Boolean vegetarian
    ) {
    }
}
