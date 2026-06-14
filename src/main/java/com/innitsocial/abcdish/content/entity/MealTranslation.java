package com.innitsocial.abcdish.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
        name = "meal_translations",
        uniqueConstraints = @UniqueConstraint(name = "uk_meal_translation_language", columnNames = {"meal_id", "language_code"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_id", nullable = false)
    private Long mealId;

    @Column(name = "language_code", nullable = false, length = 16)
    private String languageCode;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meal_translation_ingredients",
            joinColumns = @JoinColumn(name = "translation_id")
    )
    @Column(name = "ingredient")
    private List<String> ingredients;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meal_translation_steps",
            joinColumns = @JoinColumn(name = "translation_id")
    )
    @Column(name = "step")
    private List<String> steps;
}
