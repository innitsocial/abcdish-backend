CREATE SCHEMA IF NOT EXISTS abcdish;

SET search_path TO abcdish;

CREATE TABLE IF NOT EXISTS categories (
                                          id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    color_code VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS meals (
                                     id BIGSERIAL PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url TEXT,
    video_url TEXT,
    duration INTEGER,
    complexity VARCHAR(50),
    affordability VARCHAR(50),
    gluten_free BOOLEAN DEFAULT FALSE,
    lactose_free BOOLEAN DEFAULT FALSE,
    vegan BOOLEAN DEFAULT FALSE,
    vegetarian BOOLEAN DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS meal_categories (
                                               meal_id BIGINT NOT NULL,
                                               category_id VARCHAR(100) NOT NULL,
    PRIMARY KEY (meal_id, category_id),
    CONSTRAINT fk_meal_categories_meal
    FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_categories_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS meal_ingredients (
                                                meal_id BIGINT NOT NULL,
                                                ingredient TEXT NOT NULL,
                                                CONSTRAINT fk_meal_ingredients_meal
                                                FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS meal_steps (
                                          meal_id BIGINT NOT NULL,
                                          step TEXT NOT NULL,
                                          CONSTRAINT fk_meal_steps_meal
                                          FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS app_users (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    mobile_number VARCHAR(50) UNIQUE,
    password_hash TEXT,
    email_verified BOOLEAN DEFAULT FALSE,
    mobile_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50) DEFAULT 'USER',
    membership_status VARCHAR(50) DEFAULT 'FREE',
    monthly_video_views INTEGER DEFAULT 0,
    newsletter_subscribed BOOLEAN DEFAULT FALSE,
    email_marketing_enabled BOOLEAN DEFAULT FALSE,
    sms_marketing_enabled BOOLEAN DEFAULT FALSE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS otp_codes (
                                         id BIGSERIAL PRIMARY KEY,
                                         destination VARCHAR(255) NOT NULL,
    otp VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_otp_destination
    ON otp_codes(destination);

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGSERIAL PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              token VARCHAR(500) NOT NULL UNIQUE,
    revoked BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_refresh_user
    ON refresh_tokens(user_id);

CREATE TABLE IF NOT EXISTS user_sessions (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             device_name TEXT,
                                             ip_address VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    last_seen_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_sessions_user
    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_user_sessions_user
    ON user_sessions(user_id);

CREATE TABLE IF NOT EXISTS video_views (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           meal_id BIGINT NOT NULL,
                                           view_month DATE NOT NULL,
                                           viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT fk_video_views_user
                                           FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_video_views_meal
    FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_video_views_user
    ON video_views(user_id);

CREATE INDEX IF NOT EXISTS idx_video_views_meal
    ON video_views(meal_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_video_views_user_meal_month
    ON video_views(user_id, meal_id, view_month);