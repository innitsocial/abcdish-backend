SET search_path TO abcdish;

CREATE TABLE IF NOT EXISTS story_views (
    id BIGSERIAL PRIMARY KEY,
    story_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_story_views_story_user UNIQUE (story_id, user_id),
    CONSTRAINT fk_story_views_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    CONSTRAINT fk_story_views_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_story_views_story_id
    ON story_views(story_id);

CREATE TABLE IF NOT EXISTS story_likes (
    id BIGSERIAL PRIMARY KEY,
    story_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_story_likes_story_user UNIQUE (story_id, user_id),
    CONSTRAINT fk_story_likes_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    CONSTRAINT fk_story_likes_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_story_likes_story_id
    ON story_likes(story_id);
