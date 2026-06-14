ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS recipe_code VARCHAR(16);

UPDATE abcdish.meals
SET recipe_code = (10000 + id)::TEXT
WHERE recipe_code IS NULL OR recipe_code = '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_meals_recipe_code
    ON abcdish.meals (recipe_code)
    WHERE recipe_code IS NOT NULL;
