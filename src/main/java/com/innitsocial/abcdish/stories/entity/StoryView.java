package com.innitsocial.abcdish.stories.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "story_views",
        uniqueConstraints = @UniqueConstraint(name = "uk_story_views_story_user", columnNames = {"story_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime viewedAt;

    @PrePersist
    void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
