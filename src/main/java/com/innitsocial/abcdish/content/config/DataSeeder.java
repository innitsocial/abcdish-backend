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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDateTime;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MealRepository mealRepository;
    private final ContestRepository contestRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        repairOtpPurposeConstraint();
        repairModerationColumns();
        repairStoryEngagementTables();
        repairContestAcceptanceTables();
        repairMealTranslationTables();

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

        if (mealRepository.count() == 0) {

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
}
