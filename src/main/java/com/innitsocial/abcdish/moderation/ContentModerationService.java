package com.innitsocial.abcdish.moderation;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ContentModerationService {

    private static final Set<String> FOOD_TERMS = Set.of(
            "food", "cook", "cooking", "recipe", "dish", "meal", "kitchen",
            "ingredient", "ingredients", "bake", "baking", "grill", "roast",
            "fry", "boil", "simmer", "breakfast", "lunch", "dinner", "snack",
            "dessert", "chicken", "rice", "pasta", "curry", "bread", "cake",
            "sauce", "soup", "salad", "vegetable", "vegan", "vegetarian",
            "spice", "masala", "paneer", "pizza", "noodle", "fish", "meat",
            "chef", "taste", "tasty", "healthy", "protein"
    );

    private static final Set<String> BLOCKED_TERMS = Set.of(
            "porn", "nude", "nudes", "sex", "sexual", "escort", "weapon",
            "gun", "bomb", "terror", "kill", "suicide", "cocaine", "heroin",
            "meth", "hate", "racist", "scam"
    );

    public ModerationResult moderateFoodPost(List<String> textParts) {
        String text = String.join(" ", textParts == null ? List.of() : textParts)
                .toLowerCase(Locale.ROOT);

        if (text.isBlank()) {
            return new ModerationResult(
                    ModerationStatus.PENDING_REVIEW,
                    "Needs review because no text was supplied."
            );
        }

        for (String blockedTerm : BLOCKED_TERMS) {
            if (containsWord(text, blockedTerm)) {
                return new ModerationResult(
                        ModerationStatus.REJECTED,
                        "Rejected by automatic safety check."
                );
            }
        }

        for (String foodTerm : FOOD_TERMS) {
            if (containsWord(text, foodTerm)) {
                return new ModerationResult(
                        ModerationStatus.APPROVED,
                        "Approved by automatic food-content check."
                );
            }
        }

        return new ModerationResult(
                ModerationStatus.PENDING_REVIEW,
                "Needs review because the content is not clearly food related."
        );
    }

    private boolean containsWord(String text, String word) {
        return text.matches(".*\\b" + java.util.regex.Pattern.quote(word) + "\\b.*");
    }
}
