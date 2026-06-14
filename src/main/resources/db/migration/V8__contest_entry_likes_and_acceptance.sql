ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS accepted_meal_id BIGINT;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS duration INTEGER DEFAULT 30;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS complexity VARCHAR(255) DEFAULT 'simple';

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS gluten_free BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS lactose_free BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS vegan BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS vegetarian BOOLEAN DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS abcdish.contest_entry_likes (
    id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contest_entry_likes_entry_user UNIQUE (entry_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_contest_entry_likes_entry_id
ON abcdish.contest_entry_likes(entry_id);

CREATE INDEX IF NOT EXISTS idx_contest_entry_likes_user_id
ON abcdish.contest_entry_likes(user_id);
