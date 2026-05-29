package com.innitsocial.abcdish.config;

import com.innitsocial.abcdish.entity.Category;
import com.innitsocial.abcdish.entity.Meal;
import com.innitsocial.abcdish.repository.CategoryRepository;
import com.innitsocial.abcdish.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MealRepository mealRepository;

    @Override
    public void run(String... args) {

        if (categoryRepository.count() == 0) {

            List<Category> categories = List.of(
                    Category.builder()
                            .id("c1")
                            .title("Indian")
                            .colorCode("#f54242")
                            .build(),

                    Category.builder()
                            .id("c2")
                            .title("Italian")
                            .colorCode("#f5428d")
                            .build(),

                    Category.builder()
                            .id("c3")
                            .title("Quick & Easy")
                            .colorCode("#f5a442")
                            .build(),

                    Category.builder()
                            .id("c4")
                            .title("Breakfast")
                            .colorCode("#f5d142")
                            .build(),

                    Category.builder()
                            .id("c5")
                            .title("Healthy")
                            .colorCode("#368dff")
                            .build()
            );

            categoryRepository.saveAll(categories);
        }

        if (mealRepository.count() == 0) {

            List<Meal> meals = List.of(

                    Meal.builder()
                            .title("Butter Chicken")
                            .description("Classic creamy Indian butter chicken recipe.")
                            .imageUrl("https://images.unsplash.com/photo-1603894584373-5ac82b2ae398")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .duration(35)
                            .complexity("medium")
                            .affordability("affordable")
                            .categories(List.of("c1"))
                            .ingredients(List.of(
                                    "Chicken",
                                    "Butter",
                                    "Cream",
                                    "Tomatoes",
                                    "Spices"
                            ))
                            .steps(List.of(
                                    "Marinate chicken",
                                    "Cook chicken",
                                    "Prepare sauce",
                                    "Mix together",
                                    "Serve with rice"
                            ))
                            .glutenFree(true)
                            .lactoseFree(false)
                            .vegetarian(false)
                            .vegan(false)
                            .build(),

                    Meal.builder()
                            .title("Healthy Avocado Toast")
                            .description("Quick healthy breakfast with avocado.")
                            .imageUrl("https://images.unsplash.com/photo-1541519227354-08fa5d50c44d")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .duration(10)
                            .complexity("simple")
                            .affordability("affordable")
                            .categories(List.of("c4", "c5"))
                            .ingredients(List.of(
                                    "Bread",
                                    "Avocado",
                                    "Salt",
                                    "Pepper",
                                    "Lemon"
                            ))
                            .steps(List.of(
                                    "Toast bread",
                                    "Mash avocado",
                                    "Spread avocado",
                                    "Season and serve"
                            ))
                            .glutenFree(false)
                            .lactoseFree(true)
                            .vegetarian(true)
                            .vegan(true)
                            .build(),

                    Meal.builder()
                            .title("Quick Pasta")
                            .description("Simple Italian pasta recipe.")
                            .imageUrl("https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9")
                            .videoUrl("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
                            .duration(20)
                            .complexity("simple")
                            .affordability("affordable")
                            .categories(List.of("c2", "c3"))
                            .ingredients(List.of(
                                    "Pasta",
                                    "Tomato sauce",
                                    "Garlic",
                                    "Olive oil"
                            ))
                            .steps(List.of(
                                    "Boil pasta",
                                    "Prepare sauce",
                                    "Mix pasta and sauce",
                                    "Serve hot"
                            ))
                            .glutenFree(false)
                            .lactoseFree(true)
                            .vegetarian(true)
                            .vegan(true)
                            .build()
            );

            mealRepository.saveAll(meals);
        }

        System.out.println("ABCDish sample data seeded successfully.");
    }
}