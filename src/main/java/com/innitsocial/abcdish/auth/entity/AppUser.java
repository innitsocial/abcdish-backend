package com.innitsocial.abcdish.auth.entity;

import com.innitsocial.abcdish.membership.entity.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_app_user_mobile", columnNames = "mobile_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "password_hash")
    private String passwordHash;

    private boolean emailVerified;

    private boolean mobileVerified;

    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus;

    private Integer monthlyVideoViews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean newsletterSubscribed;
    private boolean emailMarketingEnabled;
    private boolean smsMarketingEnabled;
    private boolean pushNotificationsEnabled;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (membershipStatus == null) {
            membershipStatus = MembershipStatus.FREE;
        }

        if (monthlyVideoViews == null) {
            monthlyVideoViews = 0;
        }

        newsletterSubscribed = false;
        emailMarketingEnabled = false;
        smsMarketingEnabled = false;
        pushNotificationsEnabled = true;

        if (role == null) {
            role = UserRole.USER;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}