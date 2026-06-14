CREATE TABLE IF NOT EXISTS abcdish.meal_translations (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    language_code VARCHAR(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    CONSTRAINT uk_meal_translation_language UNIQUE (meal_id, language_code)
);

CREATE TABLE IF NOT EXISTS abcdish.meal_translation_ingredients (
    translation_id BIGINT NOT NULL,
    ingredient VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS abcdish.meal_translation_steps (
    translation_id BIGINT NOT NULL,
    step VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_meal_translations_meal_language
ON abcdish.meal_translations(meal_id, language_code);
