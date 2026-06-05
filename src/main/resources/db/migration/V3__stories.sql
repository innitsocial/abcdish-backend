SET search_path TO abcdish;

CREATE TABLE IF NOT EXISTS stories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    caption TEXT,
    image_url TEXT,
    video_url TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stories_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stories_created_at
    ON stories(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_stories_expires_at
    ON stories(expires_at);
