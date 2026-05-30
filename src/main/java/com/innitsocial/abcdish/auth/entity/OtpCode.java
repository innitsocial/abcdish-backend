package com.innitsocial.abcdish.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destination;

    private String otp;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    private boolean used;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}