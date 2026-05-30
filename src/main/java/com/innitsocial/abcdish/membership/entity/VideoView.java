package com.innitsocial.abcdish.membership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long mealId;

    private LocalDate viewMonth;

    private LocalDateTime viewedAt;

    @PrePersist
    void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}