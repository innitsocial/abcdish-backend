package com.innitsocial.abcdish.contest.entity;

import com.innitsocial.abcdish.moderation.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contestId;

    private Long userId;

    private String title;

    @Column(length = 2000)
    private String description;

    private String videoUrl;

    private String thumbnailUrl;

    private long votes;

    private boolean approved;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.PENDING_REVIEW;

    @Column(length = 1000)
    private String moderationReason;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
