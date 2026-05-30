package com.innitsocial.abcdish.shopping.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String ingredientName;

    private String quantity;

    private boolean purchased;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        purchased = false;
    }
}