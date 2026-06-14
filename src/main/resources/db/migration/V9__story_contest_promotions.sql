ALTER TABLE IF EXISTS abcdish.stories
ADD COLUMN IF NOT EXISTS contest_entry_id BIGINT;

ALTER TABLE IF EXISTS abcdish.stories
ADD COLUMN IF NOT EXISTS promoted_video_title VARCHAR(255);
