package com.innitsocial.abcdish.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innitsocial.abcdish.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(100)
@ConditionalOnProperty(prefix = "app.ai.recipe-generation", name = "enabled", havingValue = "true")
public class RecipeIdeaGenerationRunner implements CommandLineRunner {

    private static final String DUMMY_VIDEO_URL = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MediaService mediaService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${app.ai.openai.image-model:gpt-image-1}")
    private String imageModel;

    @Value("${app.ai.recipe-generation.limit:10}")
    private int limit;

    @Value("${app.ai.recipe-generation.generate-images:true}")
    private boolean generateImages;

    @Value("${app.ai.recipe-generation.generate-videos:false}")
    private boolean generateVideos;

    @Override
    public void run(String... args) {
        int batchLimit = Math.max(1, Math.min(limit, 50));
        if (generateImages && clean(openAiApiKey).isBlank()) {
            log.warn("Recipe generation is enabled, but OPENAI_API_KEY is missing. Skipping generation.");
            return;
        }

        List<RecipeIdeaRow> rows = jdbcTemplate.query("""
                        SELECT id,
                               recipe_name,
                               image_prompt,
                               video_generation_prompt,
                               dummy_video_url,
                               dummy_trailer_url
                        FROM abcdish.recipe_ideas
                        WHERE COALESCE(NULLIF(video_status, ''), 'READY_FOR_AI_GENERATION') = 'READY_FOR_AI_GENERATION'
                        ORDER BY id
                        LIMIT ?
                        """,
                (rs, rowNum) -> new RecipeIdeaRow(
                        rs.getLong("id"),
                        rs.getString("recipe_name"),
                        rs.getString("image_prompt"),
                        rs.getString("video_generation_prompt"),
                        rs.getString("dummy_video_url"),
                        rs.getString("dummy_trailer_url")
                ),
                batchLimit
        );

        if (rows.isEmpty()) {
            log.info("No recipe ideas are ready for AI generation.");
            return;
        }

        log.info("Starting ABCDish recipe idea generation batch size={} generateImages={} generateVideos={}",
                rows.size(), generateImages, generateVideos);

        for (RecipeIdeaRow row : rows) {
            generateRecipeIdea(row);
        }
    }

    private void generateRecipeIdea(RecipeIdeaRow row) {
        try {
            updateStatus(row.id(), "IMAGE_GENERATING");

            String thumbnailUrl = null;
            if (generateImages) {
                byte[] imageBytes = generateImage(row.imagePrompt());
                thumbnailUrl = mediaService.uploadGeneratedMedia(
                        "recipe-ideas/" + row.id() + "/thumbnail.png",
                        imageBytes,
                        "image/png"
                );
            }

            if (generateVideos) {
                updateStatus(row.id(), "VIDEO_GENERATION_PENDING");
                log.warn("Real video generation is intentionally not automatic yet for recipeIdeaId={}. " +
                        "Keep AI_RECIPE_GENERATE_VIDEOS=false until cost and account access are confirmed.", row.id());
            }

            jdbcTemplate.update("""
                            UPDATE abcdish.recipe_ideas
                            SET thumbnail_url = COALESCE(?, thumbnail_url),
                                dummy_video_url = COALESCE(NULLIF(dummy_video_url, ''), ?),
                                dummy_trailer_url = COALESCE(NULLIF(dummy_trailer_url, ''), ?),
                                video_status = ?
                            WHERE id = ?
                            """,
                    thumbnailUrl,
                    DUMMY_VIDEO_URL,
                    DUMMY_VIDEO_URL,
                    generateVideos ? "VIDEO_GENERATION_PENDING" : "DUMMY_VIDEO_READY",
                    row.id()
            );

            log.info("Generated recipe idea assets id={} recipe={} status={}",
                    row.id(), row.recipeName(), generateVideos ? "VIDEO_GENERATION_PENDING" : "DUMMY_VIDEO_READY");
        } catch (Exception error) {
            jdbcTemplate.update("""
                            UPDATE abcdish.recipe_ideas
                            SET video_status = 'FAILED'
                            WHERE id = ?
                            """,
                    row.id()
            );
            log.warn("Recipe idea generation failed id={} recipe={}: {}",
                    row.id(), row.recipeName(), error.getMessage());
        }
    }

    private byte[] generateImage(String imagePrompt) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", imageModel);
        body.put("prompt", clean(imagePrompt));
        body.put("size", "1024x1024");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(openAiBaseUrl) + "/images/generations"))
                .timeout(Duration.ofMinutes(3))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI image generation failed HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode firstImage = objectMapper.readTree(response.body()).path("data").path(0);
        String b64Json = clean(firstImage.path("b64_json").asText());
        if (!b64Json.isBlank()) {
            return Base64.getDecoder().decode(b64Json);
        }

        String imageUrl = clean(firstImage.path("url").asText());
        if (!imageUrl.isBlank()) {
            return downloadBytes(imageUrl);
        }

        throw new IOException("OpenAI image response did not include b64_json or url");
    }

    private byte[] downloadBytes(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Image download failed HTTP " + response.statusCode());
        }
        return response.body();
    }

    private void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE abcdish.recipe_ideas SET video_status = ? WHERE id = ?", status, id);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimTrailingSlash(String value) {
        String cleaned = clean(value);
        while (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    private record RecipeIdeaRow(
            Long id,
            String recipeName,
            String imagePrompt,
            String videoGenerationPrompt,
            String dummyVideoUrl,
            String dummyTrailerUrl
    ) {
    }
}
