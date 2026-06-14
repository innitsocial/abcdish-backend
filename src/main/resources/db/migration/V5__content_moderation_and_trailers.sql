ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS trailer_url VARCHAR(1000);

ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS trailer_type VARCHAR(255);

ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS promo_trailer_title VARCHAR(255);

ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS promo_trailer_subtitle VARCHAR(1000);

ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'APPROVED';

ALTER TABLE IF EXISTS abcdish.meals
    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000);

UPDATE abcdish.meals
SET moderation_status = 'APPROVED'
WHERE moderation_status IS NULL;

ALTER TABLE IF EXISTS abcdish.stories
    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'PENDING_REVIEW';

ALTER TABLE IF EXISTS abcdish.stories
    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000);

UPDATE abcdish.stories
SET moderation_status = 'APPROVED'
WHERE moderation_status IS NULL;

ALTER TABLE IF EXISTS abcdish.contest_entries
    ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(255) DEFAULT 'PENDING_REVIEW';

ALTER TABLE IF EXISTS abcdish.contest_entries
    ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(1000);

UPDATE abcdish.contest_entries
SET moderation_status = CASE WHEN approved = true THEN 'APPROVED' ELSE 'PENDING_REVIEW' END
WHERE moderation_status IS NULL;
