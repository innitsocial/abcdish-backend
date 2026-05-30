package com.innitsocial.abcdish.contest.entity;

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

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
