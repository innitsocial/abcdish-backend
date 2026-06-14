CREATE TABLE IF NOT EXISTS abcdish.recipe_ideas (
    id BIGSERIAL PRIMARY KEY,
    section VARCHAR(255) NOT NULL,
    filter_category VARCHAR(255) NOT NULL,
    recipe_name VARCHAR(255) NOT NULL,
    region_cuisine VARCHAR(255),
    key_ingredients VARCHAR(1000),
    launch_notes VARCHAR(1000),
    suggested_tags VARCHAR(1000),
    source VARCHAR(255) DEFAULT 'ABCDish seed CSV',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_recipe_ideas_section_filter_recipe UNIQUE (section, filter_category, recipe_name)
);

CREATE INDEX IF NOT EXISTS idx_recipe_ideas_filter_category
ON abcdish.recipe_ideas(filter_category);

CREATE INDEX IF NOT EXISTS idx_recipe_ideas_section
ON abcdish.recipe_ideas(section);
