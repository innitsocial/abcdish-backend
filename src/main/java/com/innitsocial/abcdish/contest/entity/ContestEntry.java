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

    @Column(name = "accepted_meal_id")
    private Long acceptedMealId;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    private Integer duration;

    private String complexity;

    private boolean glutenFree;

    private boolean lactoseFree;

    private boolean vegan;

    private boolean vegetarian;

    @Column(name = "competition_category")
    @Builder.Default
    private String competitionCategory = "main";

    @Column(name = "eligible_for_voting")
    private boolean eligibleForVoting;

    @Column(name = "competition_status")
    @Builder.Default
    private String competitionStatus = "PENDING_ADMIN_REVIEW";

    @Column(name = "finalist_rank")
    private Integer finalistRank;

    @Column(name = "london_qualified")
    private boolean londonQualified;

    @Column(name = "prize_amount_gbp")
    private Integer prizeAmountGbp;

    @Column(name = "winner_selected_at")
    private LocalDateTime winnerSelectedAt;

    @Column(name = "sound_free_confirmed")
    private boolean soundFreeConfirmed;

    @Column(name = "ai_narration_requested")
    private boolean aiNarrationRequested;

    @Column(name = "narration_status")
    @Builder.Default
    private String narrationStatus = "PENDING_REVIEW";

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
