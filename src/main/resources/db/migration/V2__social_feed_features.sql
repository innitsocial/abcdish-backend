SET search_path TO abcdish;

CREATE TABLE IF NOT EXISTS meal_likes (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_likes_meal
        FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_likes_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_meal_likes_meal_user UNIQUE (meal_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_meal_likes_meal
    ON meal_likes(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_likes_user
    ON meal_likes(user_id);

CREATE TABLE IF NOT EXISTS meal_comments (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_comments_meal
        FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_comments_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_meal_comments_meal
    ON meal_comments(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_comments_user
    ON meal_comments(user_id);

CREATE TABLE IF NOT EXISTS meal_shares (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_shares_meal
        FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_shares_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_meal_shares_meal
    ON meal_shares(meal_id);

CREATE TABLE IF NOT EXISTS creator_follows (
    id BIGSERIAL PRIMARY KEY,
    creator_key VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_creator_follows_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_creator_follows_creator_user UNIQUE (creator_key, user_id)
);

CREATE INDEX IF NOT EXISTS idx_creator_follows_creator
    ON creator_follows(creator_key);
