package com.innitsocial.abcdish.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innitsocial.abcdish.common.jobs.BackgroundJobService;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.entity.MealTranslation;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.content.repository.MealTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
public class MealTranslationService {

    private final MealTranslationRepository mealTranslationRepository;
    private final MealRepository mealRepository;
    private final BackgroundJobService backgroundJobService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.translation.enabled:true}")
    private boolean translationEnabled;

    @Value("${app.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4.1-mini}")
    private String openAiModel;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Transactional
    public MealTranslation translationFor(Meal meal, String requestedLanguage) {
        String languageCode = cleanLanguage(requestedLanguage);
        if ("en".equals(languageCode) || meal.getId() == null) {
            return fromMeal(meal, "en");
        }

        return mealTranslationRepository.findByMealIdAndLanguageCode(meal.getId(), languageCode)
                .orElseGet(() -> createTranslation(meal, languageCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MealTranslation cachedTranslationFor(Meal meal, String requestedLanguage) {
        String languageCode = cleanLanguage(requestedLanguage);
        if ("en".equals(languageCode) || meal.getId() == null) {
            return null;
        }

        MealTranslation translation = mealTranslationRepository.findByMealIdAndLanguageCode(meal.getId(), languageCode)
                .orElse(null);
        if (translation == null) {
            queueTranslation(meal.getId(), languageCode);
        }
        return translation;
    }

    @Transactional
    public void generateQueuedTranslation(Long mealId, String requestedLanguage) {
        String languageCode = cleanLanguage(requestedLanguage);
        if ("en".equals(languageCode) || mealId == null) {
            return;
        }

        if (mealTranslationRepository.findByMealIdAndLanguageCode(mealId, languageCode).isPresent()) {
            return;
        }

        Meal meal = mealRepository.findById(mealId)
                .map(this::initializeDetails)
                .orElseThrow(() -> new RuntimeException("Meal not found: " + mealId));
        createTranslation(meal, languageCode);
    }

    private MealTranslation createTranslation(Meal meal, String languageCode) {
        if (!translationEnabled || openAiApiKey == null || openAiApiKey.isBlank()) {
            return fromMeal(meal, languageCode);
        }

        try {
            AiMealTranslation translated = translateWithOpenAi(meal, languageCode);
            return mealTranslationRepository.save(MealTranslation.builder()
                    .mealId(meal.getId())
                    .languageCode(languageCode)
                    .title(cleanOrDefault(translated.title(), meal.getTitle()))
                    .description(cleanOrDefault(translated.description(), meal.getDescription()))
                    .ingredients(nonEmpty(translated.ingredients(), meal.getIngredients()))
                    .steps(nonEmpty(translated.steps(), meal.getSteps()))
                    .build());
        } catch (Exception error) {
            log.warn("Meal translation failed for meal={} language={}: {}",
                    meal.getId(), languageCode, error.getMessage());
            return fromMeal(meal, languageCode);
        }
    }

    private Meal initializeDetails(Meal meal) {
        meal.getCategories().size();
        meal.getIngredients().size();
        meal.getSteps().size();
        return meal;
    }

    private AiMealTranslation translateWithOpenAi(Meal meal, String languageCode)
            throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", openAiModel);
        body.put("input", List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                You translate cooking recipe content for ABCDish.
                                Preserve meaning, quantities, ingredient names, cooking order, and safety.
                                Do not add new ingredients, claims, timings, or cultural notes.
                                Return only translated user-facing text.
                                """
                ),
                Map.of(
                        "role", "user",
                        "content", """
                                Target language code: %s
                                Title: %s
                                Description: %s
                                Ingredients: %s
                                Steps: %s
                                """
                                .formatted(
                                        languageCode,
                                        clean(meal.getTitle()),
                                        clean(meal.getDescription()),
                                        meal.getIngredients() == null ? List.of() : meal.getIngredients(),
                                        meal.getSteps() == null ? List.of() : meal.getSteps()
                                )
                )
        ));
        body.put("text", Map.of("format", structuredOutputFormat()));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openAiBaseUrl.replaceAll("/$", "") + "/responses"))
                .timeout(Duration.ofSeconds(35))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI responded with HTTP " + response.statusCode());
        }

        return objectMapper.readValue(extractOutputText(response.body()), AiMealTranslation.class);
    }

    private Map<String, Object> structuredOutputFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("title", "description", "ingredients", "steps"));
        schema.put("properties", Map.of(
                "title", Map.of("type", "string"),
                "description", Map.of("type", "string"),
                "ingredients", Map.of("type", "array", "items", Map.of("type", "string")),
                "steps", Map.of("type", "array", "items", Map.of("type", "string"))
        ));

        return Map.of(
                "type", "json_schema",
                "name", "abcdish_meal_translation",
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

    private MealTranslation fromMeal(Meal meal, String languageCode) {
        return MealTranslation.builder()
                .mealId(meal.getId())
                .languageCode(languageCode)
                .title(meal.getTitle())
                .description(meal.getDescription())
                .ingredients(meal.getIngredients())
                .steps(meal.getSteps())
                .build();
    }

    private String cleanLanguage(String value) {
        String cleaned = clean(value).toLowerCase();
        return cleaned.isBlank() ? "en" : cleaned.split("[_-]")[0];
    }

    private String cleanOrDefault(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void queueTranslation(Long mealId, String languageCode) {
        backgroundJobService.enqueue(
                "TRANSLATE_MEAL",
                "translate-meal:%d:%s".formatted(mealId, languageCode),
                Map.of(
                        "mealId", mealId,
                        "languageCode", languageCode
                )
        );
    }

    private List<String> nonEmpty(List<String> value, List<String> fallback) {
        if (value == null) return fallback == null ? List.of() : fallback;
        List<String> cleaned = value.stream()
                .map(this::clean)
                .filter(item -> !item.isBlank())
                .toList();
        return cleaned.isEmpty() ? fallback == null ? List.of() : fallback : cleaned;
    }

    private record AiMealTranslation(
            String title,
            String description,
            List<String> ingredients,
            List<String> steps
    ) {
    }
}
