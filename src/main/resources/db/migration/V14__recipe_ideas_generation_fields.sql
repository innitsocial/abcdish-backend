ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(1000);

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS image_prompt TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS dummy_video_url VARCHAR(1000);

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS dummy_trailer_url VARCHAR(1000);

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS caption_text TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS ai_narration_script TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS background_music_style VARCHAR(255);

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS color_palette VARCHAR(255);

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS video_generation_prompt TEXT;

ALTER TABLE IF EXISTS abcdish.recipe_ideas
ADD COLUMN IF NOT EXISTS video_status VARCHAR(255) DEFAULT 'READY_FOR_AI_GENERATION';
