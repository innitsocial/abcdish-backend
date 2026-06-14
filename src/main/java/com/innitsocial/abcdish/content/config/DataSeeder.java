package com.innitsocial.abcdish.content.config;

import com.innitsocial.abcdish.content.entity.Category;
import com.innitsocial.abcdish.content.entity.Meal;
import com.innitsocial.abcdish.content.repository.CategoryRepository;
import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.contest.entity.Contest;
import com.innitsocial.abcdish.contest.entity.ContestStatus;
import com.innitsocial.abcdish.contest.repository.ContestRepository;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Order(10)
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MealRepository mealRepository;
    private final ContestRepository contestRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.recipe-ideas.reset:false}")
    private boolean resetRecipeIdeas;

    @Value("${app.seed.recipe-ideas.publish-ready-to-feed:true}")
    private boolean publishReadyRecipeIdeasToFeed;

    @Value("${app.seed.recipe-ideas.publish-all-to-feed:true}")
    private boolean publishAllRecipeIdeasToFeed;

    @Value("${app.seed.recipe-ideas.reset-categories:false}")
    private boolean resetRecipeIdeaCategories;

    @Value("${app.seed.sample-feed.enabled:false}")
    private boolean sampleFeedEnabled;

    @Value("${app.seed.sample-feed.reset:false}")
    private boolean resetSampleFeed;

    @Override
    public void run(String... args) {
        repairOtpPurposeConstraint();
        repairModerationColumns();
        repairStoryEngagementTables();
        repairContestAcceptanceTables();
        repairMealTranslationTables();
        repairRecipeIdeaTables();
        importRecipeIdeas();
        seedRecipeIdeaCategories();
        if (resetSampleFeed) {
            deleteSampleFeedMeals();
        }
        if (publishReadyRecipeIdeasToFeed) {
            publishReadyRecipeIdeasToFeed();
        }

        if (categoryRepository.count() == 0) {

            List<Category> categories = List.of(
                    Category.builder()
                            .id("c1")
                            .title("Indian")
                            .colorCode("#f54242")
                            .build(),

                    Category.builder()
                            .id("c2")
                            .title("Italian")
                            .colorCode("#f5428d")
                            .build(),

                    Category.builder()
                            .id("c3")
                            .title("Quick & Easy")
                            .colorCode("#f5a442")
                            .build(),

                    Category.builder()
                            .id("c4")
                            .title("Breakfast")
                            .colorCode("#f5d142")
                            .build(),

                    Category.builder()
                            .id("c5")
                            .title("Healthy")
                            .colorCode("#368dff")
                            .build()
            );

            categoryRepository.saveAll(categories);
        }

        if (sampleFeedEnabled && mealRepository.count() == 0) {

            List<Meal> meals = List.of(

                    Meal.builder()
                            .title("Butter Chicken")
                            .description("Classic creamy Indian butter chicken recipe.")
                            .imageUrl("https://images.unsplash.com/photo-1603894584373-5ac82b2ae398")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerType("VIDEO")
                            .duration(35)
                            .complexity("medium")
                            .affordability("affordable")
                            .categories(List.of("c1"))
                            .ingredients(List.of(
                                    "Chicken",
                                    "Butter",
                                    "Cream",
                                    "Tomatoes",
                                    "Spices"
                            ))
                            .steps(List.of(
                                    "Marinate chicken",
                                    "Cook chicken",
                                    "Prepare sauce",
                                    "Mix together",
                                    "Serve with rice"
                            ))
                            .glutenFree(true)
                            .lactoseFree(false)
                            .vegetarian(false)
                            .vegan(false)
                            .moderationStatus(ModerationStatus.APPROVED)
                            .moderationReason("Seeded recipe")
                            .build(),

                    Meal.builder()
                            .title("Healthy Avocado Toast")
                            .description("Quick healthy breakfast with avocado.")
                            .imageUrl("https://images.unsplash.com/photo-1541519227354-08fa5d50c44d")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerType("VIDEO")
                            .duration(10)
                            .complexity("simple")
                            .affordability("affordable")
                            .categories(List.of("c4", "c5"))
                            .ingredients(List.of(
                                    "Bread",
                                    "Avocado",
                                    "Salt",
                                    "Pepper",
                                    "Lemon"
                            ))
                            .steps(List.of(
                                    "Toast bread",
                                    "Mash avocado",
                                    "Spread avocado",
                                    "Season and serve"
                            ))
                            .glutenFree(false)
                            .lactoseFree(true)
                            .vegetarian(true)
                            .vegan(true)
                            .moderationStatus(ModerationStatus.APPROVED)
                            .moderationReason("Seeded recipe")
                            .build(),

                    Meal.builder()
                            .title("Quick Pasta")
                            .description("Simple Italian pasta recipe.")
                            .imageUrl("https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .trailerType("VIDEO")
                            .duration(20)
                            .complexity("simple")
                            .affordability("affordable")
                            .categories(List.of("c2", "c3"))
                            .ingredients(List.of(
                                    "Pasta",
                                    "Tomato sauce",
                                    "Garlic",
                                    "Olive oil"
                            ))
                            .steps(List.of(
                                    "Boil pasta",
                                    "Prepare sauce",
                                    "Mix pasta and sauce",
                                    "Serve hot"
                            ))
                            .glutenFree(false)
                            .lactoseFree(true)
                            .vegetarian(true)
                            .vegan(true)
                            .moderationStatus(ModerationStatus.APPROVED)
                            .moderationReason("Seeded recipe")
                            .build()
            );

            mealRepository.saveAll(meals);
            backfillRecipeCodes();
        }

        if (contestRepository.findByStatus(ContestStatus.OPEN).isEmpty()) {
            contestRepository.save(Contest.builder()
                    .title("Any Buddy Can Dish Quarterly Challenge")
                    .description("Upload your best cooking video. Community likes unlock admin review, and standout recipes can be accepted into the ABCDish video recipe database after due diligence.")
                    .prizeDescription("London quarterly cook-off invitation")
                    .status(ContestStatus.OPEN)
                    .startsAt(LocalDateTime.now())
                    .endsAt(LocalDateTime.now().plusMonths(3))
                    .build());
        }

        log.info("ABCDish sample data seeded successfully.");
    }

    private void deleteSampleFeedMeals() {
        try {
            jdbcTemplate.update("""
                    DELETE FROM abcdish.meals
                    WHERE title IN (
                        'Butter Chicken',
                        'Healthy Avocado Toast',
                        'Quick Pasta'
                    )
                    """);
            log.info("Removed old ABCDish sample feed meals");
        } catch (DataAccessException error) {
            log.warn("Could not remove old ABCDish sample feed meals", error);
        }
    }

    private void seedRecipeIdeaCategories() {
        try {
            if (resetRecipeIdeaCategories) {
                jdbcTemplate.execute("""
                        DELETE FROM abcdish.categories
                        WHERE id LIKE 'ri-section-%'
                           OR id LIKE 'ri-filter-%'
                           OR id IN ('c1', 'c2', 'c3', 'c4', 'c5')
                        """);
                log.info("Reset ABCDish recipe idea categories");
            }

            int sections = jdbcTemplate.update("""
                    INSERT INTO abcdish.categories (id, title, color_code)
                    SELECT DISTINCT
                        'ri-section-' || LOWER(REGEXP_REPLACE(section, '[^a-zA-Z0-9]+', '-', 'g')),
                        section,
                        '#2E7D32'
                    FROM abcdish.recipe_ideas
                    WHERE COALESCE(NULLIF(section, ''), '') <> ''
                    ON CONFLICT (id) DO UPDATE SET
                        title = EXCLUDED.title,
                        color_code = EXCLUDED.color_code
                    """);

            int filters = jdbcTemplate.update("""
                    INSERT INTO abcdish.categories (id, title, color_code)
                    SELECT DISTINCT
                        'ri-filter-' || LOWER(REGEXP_REPLACE(filter_category, '[^a-zA-Z0-9]+', '-', 'g')),
                        filter_category,
                        CASE
                            WHEN section = 'Meal Moment' THEN '#F2A65A'
                            WHEN section = 'Cuisine' THEN '#D94F30'
                            WHEN section = 'Diet / Protein' THEN '#2E7D32'
                            WHEN section = 'Allergy / Intolerance' THEN '#00897B'
                            WHEN section = 'Cooking Time' THEN '#6A8DFF'
                            WHEN section = 'Difficulty' THEN '#8E5CF7'
                            WHEN section = 'Cooking Method' THEN '#795548'
                            WHEN section = 'Taste / Style' THEN '#C2185B'
                            WHEN section = 'Budget' THEN '#607D8B'
                            WHEN section = 'Health / Goal' THEN '#43A047'
                            ELSE '#2E7D32'
                        END
                    FROM abcdish.recipe_ideas
                    WHERE COALESCE(NULLIF(filter_category, ''), '') <> ''
                    ON CONFLICT (id) DO UPDATE SET
                        title = EXCLUDED.title,
                        color_code = EXCLUDED.color_code
                    """);

            log.info("Seeded ABCDish recipe idea categories sections={} filters={}", sections, filters);
        } catch (DataAccessException error) {
            log.warn("Could not seed ABCDish recipe idea categories", error);
        }
    }

    public void publishReadyRecipeIdeasToFeed() {
        try {
            int inserted = jdbcTemplate.update("""
                    INSERT INTO abcdish.meals (
                        recipe_code,
                        title,
                        description,
                        image_url,
                        video_url,
                        trailer_url,
                        trailer_type,
                        promo_trailer_title,
                        promo_trailer_subtitle,
                        duration,
                        complexity,
                        affordability,
                        gluten_free,
                        lactose_free,
                        vegan,
                        vegetarian,
                        moderation_status,
                        moderation_reason
                    )
                    SELECT
                        'RI' || id,
                        recipe_name,
                        COALESCE(
                            NULLIF(caption_text, ''),
                            recipe_name || ' - ' || COALESCE(NULLIF(region_cuisine, ''), 'Global') || '. ' || COALESCE(NULLIF(launch_notes, ''), 'ABCDish recipe from the launch catalogue.')
                        ),
                        thumbnail_url,
                        COALESCE(NULLIF(dummy_video_url, ''), 'https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4'),
                        COALESCE(NULLIF(dummy_trailer_url, ''), 'https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4'),
                        'VIDEO',
                        recipe_name,
                        COALESCE(NULLIF(caption_text, ''), 'Any Buddy Can Dish'),
                        30,
                        CASE
                            WHEN LOWER(filter_category) LIKE '%hard%' OR LOWER(filter_category) LIKE '%difficult%' THEN 'hard'
                            WHEN LOWER(filter_category) LIKE '%challenging%' THEN 'challenging'
                            ELSE 'simple'
                        END,
                        'affordable',
                        LOWER(filter_category) LIKE '%gluten%',
                        LOWER(filter_category) LIKE '%lactose%',
                        LOWER(filter_category) LIKE '%vegan%',
                        LOWER(filter_category) LIKE '%vegetarian%' OR LOWER(filter_category) LIKE '%vegan%',
                        'APPROVED',
                        'Seeded from ABCDish recipe Excel backlog'
                    FROM abcdish.recipe_ideas
                    WHERE (
                        ? = true
                        OR video_status IN ('DUMMY_VIDEO_READY', 'VIDEO_GENERATION_PENDING', 'GENERATED', 'APPROVED_FOR_FEED')
                    )
                      AND COALESCE(NULLIF(recipe_name, ''), '') <> ''
                      AND NOT EXISTS (
                          SELECT 1
                          FROM abcdish.meals meal
                          WHERE meal.recipe_code = 'RI' || abcdish.recipe_ideas.id
                      )
                    """, publishAllRecipeIdeasToFeed);

            int updated = jdbcTemplate.update("""
                    UPDATE abcdish.meals meal
                    SET title = idea.recipe_name,
                        description = COALESCE(
                            NULLIF(idea.caption_text, ''),
                            idea.recipe_name || ' - ' || COALESCE(NULLIF(idea.region_cuisine, ''), 'Global') || '. ' || COALESCE(NULLIF(idea.launch_notes, ''), 'ABCDish recipe from the launch catalogue.')
                        ),
                        image_url = idea.thumbnail_url,
                        video_url = COALESCE(NULLIF(idea.dummy_video_url, ''), 'https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4'),
                        trailer_url = COALESCE(NULLIF(idea.dummy_trailer_url, ''), 'https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4'),
                        trailer_type = 'VIDEO',
                        promo_trailer_title = idea.recipe_name,
                        promo_trailer_subtitle = COALESCE(NULLIF(idea.caption_text, ''), 'Any Buddy Can Dish'),
                        duration = 30,
                        complexity = CASE
                            WHEN LOWER(idea.filter_category) LIKE '%hard%' OR LOWER(idea.filter_category) LIKE '%difficult%' THEN 'hard'
                            WHEN LOWER(idea.filter_category) LIKE '%challenging%' THEN 'challenging'
                            ELSE 'simple'
                        END,
                        affordability = 'affordable',
                        gluten_free = LOWER(idea.filter_category) LIKE '%gluten%',
                        lactose_free = LOWER(idea.filter_category) LIKE '%lactose%',
                        vegan = LOWER(idea.filter_category) LIKE '%vegan%',
                        vegetarian = LOWER(idea.filter_category) LIKE '%vegetarian%' OR LOWER(idea.filter_category) LIKE '%vegan%',
                        moderation_status = 'APPROVED',
                        moderation_reason = 'Seeded from ABCDish recipe Excel backlog'
                    FROM abcdish.recipe_ideas idea
                    WHERE meal.recipe_code = 'RI' || idea.id
                      AND (
                          ? = true
                          OR idea.video_status IN ('DUMMY_VIDEO_READY', 'VIDEO_GENERATION_PENDING', 'GENERATED', 'APPROVED_FOR_FEED')
                      )
                    """, publishAllRecipeIdeasToFeed);

            log.info("Published ABCDish recipe ideas to feed meals inserted={} updated={}", inserted, updated);
            backfillMealDetailsForRecipeIdeas();
            backfillRecipeCodes();
        } catch (DataAccessException error) {
            log.warn("Could not publish ready ABCDish recipe ideas to feed", error);
        }
    }

    private void backfillMealDetailsForRecipeIdeas() {
        jdbcTemplate.execute("""
                DELETE FROM abcdish.meal_categories
                WHERE meal_id IN (
                    SELECT id
                    FROM abcdish.meals
                    WHERE recipe_code LIKE 'RI%'
                )
                """);

        jdbcTemplate.execute("""
                DELETE FROM abcdish.meal_ingredients
                WHERE meal_id IN (
                    SELECT id
                    FROM abcdish.meals
                    WHERE recipe_code LIKE 'RI%'
                )
                """);

        jdbcTemplate.execute("""
                DELETE FROM abcdish.meal_steps
                WHERE meal_id IN (
                    SELECT id
                    FROM abcdish.meals
                    WHERE recipe_code LIKE 'RI%'
                )
                """);

        jdbcTemplate.execute("""
                INSERT INTO abcdish.meal_categories (meal_id, category_id)
                SELECT meal.id, 'ri-section-' || LOWER(REGEXP_REPLACE(idea.section, '[^a-zA-Z0-9]+', '-', 'g'))
                FROM abcdish.meals meal
                JOIN abcdish.recipe_ideas idea ON meal.recipe_code = 'RI' || idea.id
                WHERE meal.recipe_code LIKE 'RI%'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM abcdish.meal_categories category
                      WHERE category.meal_id = meal.id
                        AND category.category_id = 'ri-section-' || LOWER(REGEXP_REPLACE(idea.section, '[^a-zA-Z0-9]+', '-', 'g'))
                  )
                """);

        jdbcTemplate.execute("""
                INSERT INTO abcdish.meal_categories (meal_id, category_id)
                SELECT meal.id, 'ri-filter-' || LOWER(REGEXP_REPLACE(idea.filter_category, '[^a-zA-Z0-9]+', '-', 'g'))
                FROM abcdish.meals meal
                JOIN abcdish.recipe_ideas idea ON meal.recipe_code = 'RI' || idea.id
                WHERE meal.recipe_code LIKE 'RI%'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM abcdish.meal_categories category
                      WHERE category.meal_id = meal.id
                        AND category.category_id = 'ri-filter-' || LOWER(REGEXP_REPLACE(idea.filter_category, '[^a-zA-Z0-9]+', '-', 'g'))
                  )
                """);

        jdbcTemplate.execute("""
                INSERT INTO abcdish.meal_ingredients (meal_id, ingredient)
                SELECT meal.id, INITCAP(TRIM(ingredient))
                FROM abcdish.meals meal
                JOIN abcdish.recipe_ideas idea ON meal.recipe_code = 'RI' || idea.id
                CROSS JOIN LATERAL REGEXP_SPLIT_TO_TABLE(COALESCE(NULLIF(idea.key_ingredients, ''), 'Verify ingredients'), ',') ingredient
                WHERE meal.recipe_code LIKE 'RI%'
                  AND COALESCE(NULLIF(TRIM(ingredient), ''), '') <> ''
                """);

        jdbcTemplate.execute("""
                INSERT INTO abcdish.meal_steps (meal_id, step)
                SELECT meal.id, step
                FROM abcdish.meals meal
                JOIN abcdish.recipe_ideas idea ON meal.recipe_code = 'RI' || idea.id
                CROSS JOIN LATERAL (
                    VALUES
                        ('Gather ingredients for ' || idea.recipe_name || ': ' || COALESCE(NULLIF(idea.key_ingredients, ''), 'verify ingredients from the recipe plan') || '.'),
                        ('Prepare the ingredients and follow the ABCDish video plan for ' || idea.recipe_name || '.'),
                        ('Cook using the selected style: ' || COALESCE(NULLIF(idea.filter_category, ''), 'ABCDish recipe') || '.'),
                        ('Finish, taste, plate, and review captions/narration before publishing. Notes: ' || COALESCE(NULLIF(idea.launch_notes, ''), 'ready for editorial review') || '.')
                ) steps(step)
                WHERE meal.recipe_code LIKE 'RI%'
                """);
    }

    private void repairOtpPurposeConstraint() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.otp_codes
                    DROP CONSTRAINT IF EXISTS otp_codes_purpose_check
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.otp_codes
                    ADD CONSTRAINT otp_codes_purpose_check
                    CHECK (purpose IN (
                        'LOGIN',
                        'REGISTER_EMAIL',
                        'VERIFY_EMAIL',
                        'VERIFY_MOBILE',
                        'RESET_PASSWORD'
                    ))
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair otp_codes purpose constraint", error);
        }
    }

    private void repairModerationColumns() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS recipe_code VARCHAR(16)
                    """);
            jdbcTemplate.execute("""
                    UPDATE abcdish.meals
                    SET recipe_code = (10000 + id)::TEXT
                    WHERE recipe_code IS NULL OR recipe_code = ''
                    """);
            jdbcTemplate.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uk_meals_recipe_code
                    ON abcdish.meals (recipe_code)
                    WHERE recipe_code IS NOT NULL
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'APPROVED'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS trailer_url VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS trailer_type VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS promo_trailer_title VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS promo_trailer_subtitle VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.meals
                    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    UPDATE abcdish.meals
                    SET moderation_status = 'APPROVED'
                    WHERE moderation_status IS NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.stories
                    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'PENDING_REVIEW'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.stories
                    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.stories
                    ADD COLUMN IF NOT EXISTS contest_entry_id BIGINT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.stories
                    ADD COLUMN IF NOT EXISTS promoted_video_title VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    UPDATE abcdish.stories
                    SET moderation_status = 'APPROVED'
                    WHERE moderation_status IS NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'PENDING_REVIEW'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    UPDATE abcdish.contest_entries
                    SET moderation_status = CASE WHEN approved = true THEN 'APPROVED' ELSE 'PENDING_REVIEW' END
                    WHERE moderation_status IS NULL
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair moderation columns", error);
        }
    }

    private void backfillRecipeCodes() {
        try {
            jdbcTemplate.execute("""
                    UPDATE abcdish.meals
                    SET recipe_code = (10000 + id)::TEXT
                    WHERE recipe_code IS NULL OR recipe_code = ''
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not backfill recipe codes", error);
        }
    }

    private void repairStoryEngagementTables() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.story_views (
                        id BIGSERIAL PRIMARY KEY,
                        story_id BIGINT NOT NULL,
                        user_id BIGINT NOT NULL,
                        viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_story_views_story_user UNIQUE (story_id, user_id)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_story_views_story_id
                    ON abcdish.story_views(story_id)
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.story_likes (
                        id BIGSERIAL PRIMARY KEY,
                        story_id BIGINT NOT NULL,
                        user_id BIGINT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_story_likes_story_user UNIQUE (story_id, user_id)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_story_likes_story_id
                    ON abcdish.story_likes(story_id)
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair story engagement tables", error);
        }
    }

    private void repairContestAcceptanceTables() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS accepted_meal_id BIGINT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS duration INTEGER DEFAULT 30
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS complexity VARCHAR(255) DEFAULT 'simple'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS gluten_free BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS lactose_free BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS vegan BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS vegetarian BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS sound_free_confirmed BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS ai_narration_requested BOOLEAN DEFAULT TRUE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS narration_status VARCHAR(255) DEFAULT 'PENDING_REVIEW'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS competition_category VARCHAR(255) DEFAULT 'main'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS eligible_for_voting BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS competition_status VARCHAR(255) DEFAULT 'PENDING_ADMIN_REVIEW'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS finalist_rank INTEGER
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS london_qualified BOOLEAN DEFAULT FALSE
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS prize_amount_gbp INTEGER
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.contest_entries
                    ADD COLUMN IF NOT EXISTS winner_selected_at TIMESTAMP
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.contest_entry_likes (
                        id BIGSERIAL PRIMARY KEY,
                        entry_id BIGINT NOT NULL,
                        user_id BIGINT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_contest_entry_likes_entry_user UNIQUE (entry_id, user_id)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_contest_entry_likes_entry_id
                    ON abcdish.contest_entry_likes(entry_id)
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_contest_entry_likes_user_id
                    ON abcdish.contest_entry_likes(user_id)
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair contest acceptance tables", error);
        }
    }

    private void repairMealTranslationTables() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.meal_translations (
                        id BIGSERIAL PRIMARY KEY,
                        meal_id BIGINT NOT NULL,
                        language_code VARCHAR(16) NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        description VARCHAR(2000),
                        CONSTRAINT uk_meal_translation_language UNIQUE (meal_id, language_code)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.meal_translation_ingredients (
                        translation_id BIGINT NOT NULL,
                        ingredient VARCHAR(255)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.meal_translation_steps (
                        translation_id BIGINT NOT NULL,
                        step VARCHAR(255)
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_meal_translations_meal_language
                    ON abcdish.meal_translations(meal_id, language_code)
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair meal translation tables", error);
        }
    }

    private void repairRecipeIdeaTables() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS abcdish.recipe_ideas (
                        id BIGSERIAL PRIMARY KEY,
                        section VARCHAR(255) NOT NULL,
                        filter_category VARCHAR(255) NOT NULL,
                        recipe_name VARCHAR(255) NOT NULL,
                        region_cuisine VARCHAR(255),
                        key_ingredients VARCHAR(1000),
                        launch_notes VARCHAR(1000),
                        suggested_tags VARCHAR(1000),
                        thumbnail_url VARCHAR(1000),
                        image_prompt TEXT,
                        dummy_video_url VARCHAR(1000),
                        dummy_trailer_url VARCHAR(1000),
                        caption_text TEXT,
                        ai_narration_script TEXT,
                        background_music_style VARCHAR(255),
                        color_palette VARCHAR(255),
                        video_generation_prompt TEXT,
                        video_status VARCHAR(255) DEFAULT 'READY_FOR_AI_GENERATION',
                        source VARCHAR(255) DEFAULT 'ABCDish seed CSV',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_recipe_ideas_section_filter_recipe UNIQUE (section, filter_category, recipe_name)
                    )
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS image_prompt TEXT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS dummy_video_url VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS dummy_trailer_url VARCHAR(1000)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS caption_text TEXT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS ai_narration_script TEXT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS background_music_style VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS color_palette VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS video_generation_prompt TEXT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE IF EXISTS abcdish.recipe_ideas
                    ADD COLUMN IF NOT EXISTS video_status VARCHAR(255) DEFAULT 'READY_FOR_AI_GENERATION'
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_recipe_ideas_filter_category
                    ON abcdish.recipe_ideas(filter_category)
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_recipe_ideas_section
                    ON abcdish.recipe_ideas(section)
                    """);
        } catch (DataAccessException error) {
            log.warn("Could not repair recipe idea tables", error);
        }
    }

    private void importRecipeIdeas() {
        try {
            ClassPathResource resource = new ClassPathResource("data/abcdish_recipe_ideas.csv");
            if (!resource.exists()) {
                log.warn("Recipe ideas CSV not found on classpath");
                return;
            }

            if (resetRecipeIdeas) {
                resetRecipeIdeasTable();
                log.info("Reset all ABCDish recipe ideas before import");
            }

            int imported = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            ))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) {
                        header = false;
                        continue;
                    }

                    List<String> row = parseCsvLine(line);
                    if (row.size() < 7 || clean(row.get(2)).isBlank()) {
                        continue;
                    }

                    String section = clean(row.get(0));
                    String filterCategory = clean(row.get(1));
                    String recipeName = clean(row.get(2));
                    String regionCuisine = clean(row.get(3));
                    String keyIngredients = clean(row.get(4));
                    String launchNotes = clean(row.get(5));
                    String suggestedTags = clean(row.get(6));
                    String slug = slugify(recipeName);

                    jdbcTemplate.update("""
                                    INSERT INTO abcdish.recipe_ideas (
                                        section,
                                        filter_category,
                                        recipe_name,
                                        region_cuisine,
                                        key_ingredients,
                                        launch_notes,
                                        suggested_tags,
                                        thumbnail_url,
                                        image_prompt,
                                        dummy_video_url,
                                        dummy_trailer_url,
                                        caption_text,
                                        ai_narration_script,
                                        background_music_style,
                                        color_palette,
                                        video_generation_prompt,
                                        video_status,
                                        source
                                    )
                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                    ON CONFLICT (section, filter_category, recipe_name) DO UPDATE SET
                                        region_cuisine = EXCLUDED.region_cuisine,
                                        key_ingredients = EXCLUDED.key_ingredients,
                                        launch_notes = EXCLUDED.launch_notes,
                                        suggested_tags = EXCLUDED.suggested_tags,
                                        thumbnail_url = COALESCE(NULLIF(abcdish.recipe_ideas.thumbnail_url, ''), EXCLUDED.thumbnail_url),
                                        image_prompt = EXCLUDED.image_prompt,
                                        dummy_video_url = COALESCE(NULLIF(abcdish.recipe_ideas.dummy_video_url, ''), EXCLUDED.dummy_video_url),
                                        dummy_trailer_url = COALESCE(NULLIF(abcdish.recipe_ideas.dummy_trailer_url, ''), EXCLUDED.dummy_trailer_url),
                                        caption_text = EXCLUDED.caption_text,
                                        ai_narration_script = EXCLUDED.ai_narration_script,
                                        background_music_style = EXCLUDED.background_music_style,
                                        color_palette = EXCLUDED.color_palette,
                                        video_generation_prompt = EXCLUDED.video_generation_prompt,
                                        video_status = COALESCE(NULLIF(abcdish.recipe_ideas.video_status, ''), EXCLUDED.video_status),
                                        source = EXCLUDED.source
                                    """,
                            section,
                            filterCategory,
                            recipeName,
                            regionCuisine,
                            keyIngredients,
                            launchNotes,
                            suggestedTags,
                            "https://picsum.photos/seed/abcdish-" + slug + "/1200/900",
                            imagePrompt(recipeName, regionCuisine, keyIngredients, filterCategory),
                            "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
                            "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
                            captionText(recipeName, regionCuisine, filterCategory),
                            aiNarrationScript(recipeName, regionCuisine, keyIngredients),
                            backgroundMusicStyle(filterCategory, section),
                            colorPalette(filterCategory, section),
                            videoGenerationPrompt(section, filterCategory, recipeName, regionCuisine, keyIngredients),
                            "READY_FOR_AI_GENERATION",
                            "ABCDish seed CSV"
                    );
                    imported++;
                }
            }

            log.info("ABCDish recipe ideas seed processed rows={}", imported);
        } catch (Exception error) {
            log.warn("Could not import recipe ideas CSV", error);
        }
    }

    private void resetRecipeIdeasTable() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE abcdish.recipe_ideas RESTART IDENTITY");
        } catch (DataAccessException error) {
            log.warn("Could not truncate recipe ideas table, falling back to delete", error);
            jdbcTemplate.update("DELETE FROM abcdish.recipe_ideas");
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char value = line.charAt(index);
            if (value == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (value == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }

        values.add(current.toString());
        return values;
    }

    private String imagePrompt(
            String recipeName,
            String regionCuisine,
            String keyIngredients,
            String filterCategory
    ) {
        return """
                Create a bright, trustworthy food thumbnail for ABCDish.
                Dish: %s.
                Cuisine/region: %s.
                Key ingredients: %s.
                Filter/category: %s.
                Show the finished dish clearly on a clean plate or bowl, natural food colors,
                appetizing but realistic, no people, no logos, no text, no clutter.
                """
                .formatted(recipeName, regionCuisine, keyIngredients, filterCategory)
                .trim();
    }

    private String captionText(String recipeName, String regionCuisine, String filterCategory) {
        return "ABCDish " + filterCategory + ": " + recipeName
                + (regionCuisine.isBlank() ? "" : " from " + regionCuisine)
                + ". Clean recipe video, clear captions, AI narration ready.";
    }

    private String aiNarrationScript(String recipeName, String regionCuisine, String keyIngredients) {
        return """
                Today on ABCDish, we are making %s%s.
                Keep the video focused on the recipe: show ingredients first, then each cooking step clearly.
                Main ingredients: %s.
                Narration should be calm, friendly, accurate, and easy to translate into every supported language.
                Mention timing, texture, heat level, and final serving cues without adding unverified health claims.
                """
                .formatted(
                        recipeName,
                        regionCuisine.isBlank() ? "" : " from " + regionCuisine,
                        keyIngredients
                )
                .trim();
    }

    private String backgroundMusicStyle(String filterCategory, String section) {
        String cleaned = (filterCategory + " " + section).toLowerCase();
        if (cleaned.contains("dessert") || cleaned.contains("sweet")) {
            return "light warm acoustic background, low volume, gentle tempo";
        }
        if (cleaned.contains("drink")) {
            return "light fresh lounge background, low volume, relaxed tempo";
        }
        if (cleaned.contains("spicy") || cleaned.contains("street")) {
            return "light upbeat world percussion, low volume, energetic but not distracting";
        }
        return "light modern kitchen background music, low volume, warm and trustworthy";
    }

    private String colorPalette(String filterCategory, String section) {
        String cleaned = (filterCategory + " " + section).toLowerCase();
        if (cleaned.contains("dessert") || cleaned.contains("sweet")) {
            return "berry, cream, cocoa, warm gold";
        }
        if (cleaned.contains("drink")) {
            return "mint, citrus, ice blue, clean white";
        }
        if (cleaned.contains("vegan") || cleaned.contains("healthy")) {
            return "fresh green, tomato red, lemon, soft white";
        }
        if (cleaned.contains("spicy") || cleaned.contains("street")) {
            return "chilli red, turmeric, charcoal, lime";
        }
        return "fresh green, warm tomato, cream, charcoal";
    }

    private String videoGenerationPrompt(
            String section,
            String filterCategory,
            String recipeName,
            String regionCuisine,
            String keyIngredients
    ) {
        return """
                Create an ABCDish production-ready cooking video for "%s".
                Section: %s. Filter/category: %s. Cuisine/region: %s.
                Key ingredients: %s.

                Requirements:
                - Recipe-focused only: ingredients, method, texture, timing, final dish.
                - No face-led storytelling, no celebrity framing, no copyrighted logos.
                - Produce a silent base video suitable for multilingual AI narration.
                - Add clear captions/subtitles area with high contrast.
                - Add light background music only under narration.
                - Include a 30-second trailer plus a full recipe video.
                - Use appetizing realistic food visuals, clean kitchen lighting, trustworthy style.
                - Output should support English plus all ABCDish app languages.
                """
                .formatted(recipeName, section, filterCategory, regionCuisine, keyIngredients)
                .trim();
    }

    private String slugify(String value) {
        String slug = clean(value).toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "recipe" : slug;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
