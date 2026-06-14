CREATE INDEX IF NOT EXISTS idx_meals_moderation_status_id
ON abcdish.meals(moderation_status, id);

CREATE INDEX IF NOT EXISTS idx_meals_recipe_code
ON abcdish.meals(recipe_code);

CREATE INDEX IF NOT EXISTS idx_meal_categories_meal_id
ON abcdish.meal_categories(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_categories_category_id
ON abcdish.meal_categories(category_id);

CREATE INDEX IF NOT EXISTS idx_meal_ingredients_meal_id
ON abcdish.meal_ingredients(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_steps_meal_id
ON abcdish.meal_steps(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_likes_meal_id
ON abcdish.meal_likes(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_likes_user_meal
ON abcdish.meal_likes(user_id, meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_comments_meal_id
ON abcdish.meal_comments(meal_id);

CREATE INDEX IF NOT EXISTS idx_meal_shares_meal_id
ON abcdish.meal_shares(meal_id);

CREATE INDEX IF NOT EXISTS idx_creator_follows_user_creator
ON abcdish.creator_follows(user_id, creator_key);

CREATE INDEX IF NOT EXISTS idx_contest_entries_feed
ON abcdish.contest_entries(moderation_status, eligible_for_voting, competition_status, votes DESC, created_at DESC);
