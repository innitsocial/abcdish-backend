ALTER TABLE IF EXISTS abcdish.meal_ingredients
ALTER COLUMN ingredient TYPE TEXT;

ALTER TABLE IF EXISTS abcdish.meal_steps
ALTER COLUMN step TYPE TEXT;

ALTER TABLE IF EXISTS abcdish.meal_translation_ingredients
ALTER COLUMN ingredient TYPE TEXT;

ALTER TABLE IF EXISTS abcdish.meal_translation_steps
ALTER COLUMN step TYPE TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS detailed_ingredients TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS detailed_steps TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS detail_status VARCHAR(255) DEFAULT 'READY_FOR_DETAIL_GENERATION';
