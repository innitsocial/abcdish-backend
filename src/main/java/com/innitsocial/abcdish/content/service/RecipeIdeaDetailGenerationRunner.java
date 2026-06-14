package com.innitsocial.abcdish.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innitsocial.abcdish.content.config.DataSeeder;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(80)
@ConditionalOnProperty(prefix = "app.ai.recipe-detail-generation", name = "enabled", havingValue = "true")
public class RecipeIdeaDetailGenerationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final DataSeeder dataSeeder;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${app.ai.recipe-detail-generation.model:gpt-4.1-mini}")
    private String detailModel;

    @Value("${app.ai.recipe-detail-generation.limit:10}")
    private int limit;

    @Override
    public void run(String... args) {
        int batchLimit = Math.max(1, Math.min(limit, 100));
        if (clean(openAiApiKey).isBlank()) {
            log.warn("Recipe detail generation is enabled, but OPENAI_API_KEY is missing. Skipping.");
            return;
        }

        List<RecipeIdeaRow> rows = jdbcTemplate.query("""
                        SELECT id,
                               section,
                               filter_category,
                               recipe_name,
                               region_cuisine,
                               key_ingredients,
                               launch_notes,
                               suggested_tags
                        FROM abcdish.recipe_ideas
                        WHERE COALESCE(NULLIF(detail_status, ''), 'READY_FOR_DETAIL_GENERATION') = 'READY_FOR_DETAIL_GENERATION'
                        ORDER BY id
                        LIMIT ?
                        """,
                (rs, rowNum) -> new RecipeIdeaRow(
                        rs.getLong("id"),
                        rs.getString("section"),
                        rs.getString("filter_category"),
                        rs.getString("recipe_name"),
                        rs.getString("region_cuisine"),
                        rs.getString("key_ingredients"),
                        rs.getString("launch_notes"),
                        rs.getString("suggested_tags")
                ),
                batchLimit
        );

        if (rows.isEmpty()) {
            log.info("No recipe ideas need detailed ingredient/step generation.");
            return;
        }

        log.info("Starting ABCDish recipe detail generation batch size={}", rows.size());
        for (RecipeIdeaRow row : rows) {
            boolean shouldContinue = generateDetails(row);
            if (!shouldContinue) {
                log.warn("Stopping ABCDish recipe detail generation batch early.");
                return;
            }
        }

        dataSeeder.publishReadyRecipeIdeasToFeed();
    }

    private boolean generateDetails(RecipeIdeaRow row) {
        try {
            jdbcTemplate.update("UPDATE abcdish.recipe_ideas SET detail_status = ? WHERE id = ?",
                    "DETAIL_GENERATING",
                    row.id()
            );

            RecipeDetails details = generateRecipeDetails(row);
            jdbcTemplate.update("""
                            UPDATE abcdish.recipe_ideas
                            SET detailed_ingredients = ?,
                                detailed_steps = ?,
                                detail_status = 'DETAIL_READY'
                            WHERE id = ?
                            """,
                    objectMapper.writeValueAsString(details.ingredients()),
                    objectMapper.writeValueAsString(details.steps()),
                    row.id()
            );

            log.info("Generated detailed recipe data id={} recipe={} ingredients={} steps={}",
                    row.id(), row.recipeName(), details.ingredients().size(), details.steps().size());
            return true;
        } catch (Exception error) {
            String message = clean(error.getMessage());
            jdbcTemplate.update("UPDATE abcdish.recipe_ideas SET detail_status = ? WHERE id = ?",
                    "DETAIL_FAILED",
                    row.id()
            );
            log.warn("Recipe detail generation failed id={} recipe={}: {}", row.id(), row.recipeName(), message);
            return !isBillingLimitError(message);
        }
    }

    private RecipeDetails generateRecipeDetails(RecipeIdeaRow row) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", detailModel);
        body.put("input", List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                You create production-quality recipe data for ABCDish.
                                Accuracy matters. Include every ingredient a real cook needs, including salt,
                                water, oil, garnish, spices, and optional serving ingredients when normally required.
                                Do not refer to videos, trailers, creators, or AI. Do not use vague placeholders.
                                Return practical home-cooking instructions with precise sequence and sensory cues.
                                """
                ),
                Map.of(
                        "role", "user",
                        "content", detailPrompt(row)
                )
        ));
        body.put("text", Map.of("format", structuredOutputFormat()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(openAiBaseUrl) + "/responses"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI recipe detail generation failed HTTP " + response.statusCode() + ": " + response.body());
        }

        RecipeDetails details = objectMapper.readValue(extractOutputText(response.body()), RecipeDetails.class);
        if (details.ingredients() == null || details.ingredients().size() < 5) {
            throw new IOException("Recipe detail response had too few ingredients");
        }
        if (details.steps() == null || details.steps().size() < 6) {
            throw new IOException("Recipe detail response had too few steps");
        }
        return details;
    }

    private String detailPrompt(RecipeIdeaRow row) {
        return """
                Recipe: %s
                Region/cuisine: %s
                ABCDish section: %s
                Filter/category: %s
                Existing key ingredients: %s
                Launch notes: %s
                Tags: %s

                Create a complete recipe for 4 servings unless the dish is normally a drink or dessert;
                then use a sensible standard serving count and state it in the ingredients where useful.

                Ingredients:
                - Return 10 to 22 complete ingredient lines where appropriate.
                - Include quantity, unit, ingredient name, preparation, and notes in each string.
                - Include salt, pepper, water, oil, spices, garnish, and serving items when needed.
                - For dietary/allergy categories, keep the recipe consistent with that category.

                Steps:
                - Return 8 to 16 detailed cooking steps.
                - Include preparation, heat level, timing, texture/colour cues, doneness checks,
                  resting/cooling, plating, and serving.
                - No video references. No "watch the video". No placeholders.
                """
                .formatted(
                        row.recipeName(),
                        cleanOrDefault(row.regionCuisine(), "Global"),
                        cleanOrDefault(row.section(), "Recipe"),
                        cleanOrDefault(row.filterCategory(), "General"),
                        cleanOrDefault(row.keyIngredients(), "Not supplied"),
                        cleanOrDefault(row.launchNotes(), "Not supplied"),
                        cleanOrDefault(row.suggestedTags(), "Not supplied")
                );
    }

    private Map<String, Object> structuredOutputFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("ingredients", "steps"));
        schema.put("properties", Map.of(
                "ingredients", Map.of(
                        "type", "array",
                        "minItems", 5,
                        "maxItems", 25,
                        "items", Map.of("type", "string")
                ),
                "steps", Map.of(
                        "type", "array",
                        "minItems", 6,
                        "maxItems", 20,
                        "items", Map.of("type", "string")
                )
        ));

        return Map.of(
                "type", "json_schema",
                "name", "abcdish_recipe_details",
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

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanOrDefault(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private boolean isBillingLimitError(String value) {
        String cleaned = clean(value).toLowerCase();
        return cleaned.contains("billing_hard_limit_reached")
                || cleaned.contains("billing hard limit")
                || cleaned.contains("billing limit");
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
            String section,
            String filterCategory,
            String recipeName,
            String regionCuisine,
            String keyIngredients,
            String launchNotes,
            String suggestedTags
    ) {
    }

    private record RecipeDetails(
            List<String> ingredients,
            List<String> steps
    ) {
    }
}
