package com.innitsocial.abcdish.stories.entity;

import com.innitsocial.abcdish.moderation.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String caption;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.PENDING_REVIEW;

    @Column(length = 1000)
    private String moderationReason;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();

        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24);
        }
    }
}
