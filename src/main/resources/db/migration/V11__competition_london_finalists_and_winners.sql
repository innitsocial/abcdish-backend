ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS competition_category VARCHAR(255) DEFAULT 'main';

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS eligible_for_voting BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS competition_status VARCHAR(255) DEFAULT 'PENDING_ADMIN_REVIEW';

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS finalist_rank INTEGER;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS london_qualified BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS prize_amount_gbp INTEGER;

ALTER TABLE IF EXISTS abcdish.contest_entries
ADD COLUMN IF NOT EXISTS winner_selected_at TIMESTAMP;
