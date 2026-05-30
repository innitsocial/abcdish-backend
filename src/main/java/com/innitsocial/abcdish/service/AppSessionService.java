package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.dto.AppSessionResponse;
import com.innitsocial.abcdish.entity.AppUser;
import com.innitsocial.abcdish.entity.MembershipStatus;
import com.innitsocial.abcdish.entity.UserRole;
import com.innitsocial.abcdish.repository.AppUserRepository;
import com.innitsocial.abcdish.repository.VideoViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AppSessionService {

    private static final int FREE_MONTHLY_LIMIT = 10;

    private final AppUserRepository appUserRepository;
    private final VideoViewRepository videoViewRepository;

    public AppSessionResponse getSession(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate month = LocalDate.now().withDayOfMonth(1);
        long usedViews = videoViewRepository.countByUserIdAndViewMonth(userId, month);

        boolean isPaid = user.getMembershipStatus() == MembershipStatus.ACTIVE;
        boolean isCreator = user.getRole() == UserRole.CREATOR;
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        long remainingViews = isPaid
                ? Long.MAX_VALUE
                : Math.max(0, FREE_MONTHLY_LIMIT - usedViews);

        var features = new AppSessionResponse.Features(
                isPaid,
                isCreator || isAdmin,
                isAdmin,
                true,
                !isPaid
        );

        return new AppSessionResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                user.getRole().name(),
                user.getMembershipStatus().name(),
                (int) usedViews,
                remainingViews,
                features
        );
    }
}