package com.innitsocial.abcdish.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "meals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    private Integer duration;

    private String complexity;

    private String affordability;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meal_categories",
            joinColumns = @JoinColumn(name = "meal_id")
    )
    @Column(name = "category_id")
    private List<String> categories;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meal_ingredients",
            joinColumns = @JoinColumn(name = "meal_id")
    )
    @Column(name = "ingredient")
    private List<String> ingredients;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meal_steps",
            joinColumns = @JoinColumn(name = "meal_id")
    )
    @Column(name = "step")
    private List<String> steps;

    private boolean glutenFree;

    private boolean lactoseFree;

    private boolean vegan;

    private boolean vegetarian;
}