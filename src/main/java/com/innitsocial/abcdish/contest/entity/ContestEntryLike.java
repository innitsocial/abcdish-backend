package com.innitsocial.abcdish.contest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contest_entry_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contest_entry_likes_entry_user",
                columnNames = {"entry_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestEntryLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
