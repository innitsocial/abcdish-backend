package com.innitsocial.abcdish.social.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_id", nullable = false)
    private Long mealId;

    @Column(name = "user_id")
    private Long userId;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
