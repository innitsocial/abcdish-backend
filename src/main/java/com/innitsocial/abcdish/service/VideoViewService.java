package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.dto.VideoAccessResponse;
import com.innitsocial.abcdish.entity.AppUser;
import com.innitsocial.abcdish.entity.MembershipStatus;
import com.innitsocial.abcdish.entity.VideoView;
import com.innitsocial.abcdish.repository.AppUserRepository;
import com.innitsocial.abcdish.repository.VideoViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VideoViewService {

    private static final int FREE_MONTHLY_LIMIT = 10;

    private final VideoViewRepository videoViewRepository;
    private final AppUserRepository appUserRepository;

    public VideoAccessResponse recordView(Long userId, Long mealId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate month = LocalDate.now().withDayOfMonth(1);

        long usedViews = videoViewRepository.countByUserIdAndViewMonth(userId, month);

        if (user.getMembershipStatus() == MembershipStatus.ACTIVE) {
            saveViewIfNew(userId, mealId, month);

            return new VideoAccessResponse(
                    true,
                    "Unlimited access",
                    usedViews,
                    Long.MAX_VALUE,
                    user.getMembershipStatus().name()
            );
        }

        var existingView = videoViewRepository.findByUserIdAndMealIdAndViewMonth(
                userId,
                mealId,
                month
        );

        if (existingView.isPresent()) {
            return new VideoAccessResponse(
                    true,
                    "Already counted this month",
                    usedViews,
                    FREE_MONTHLY_LIMIT - usedViews,
                    user.getMembershipStatus().name()
            );
        }

        if (usedViews >= FREE_MONTHLY_LIMIT) {
            return new VideoAccessResponse(
                    false,
                    "Free monthly video limit reached",
                    usedViews,
                    0,
                    user.getMembershipStatus().name()
            );
        }

        saveViewIfNew(userId, mealId, month);

        return new VideoAccessResponse(
                true,
                "Video view allowed",
                usedViews + 1,
                FREE_MONTHLY_LIMIT - usedViews - 1,
                user.getMembershipStatus().name()
        );
    }

    public VideoAccessResponse usage(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate month = LocalDate.now().withDayOfMonth(1);
        long usedViews = videoViewRepository.countByUserIdAndViewMonth(userId, month);

        long remaining = user.getMembershipStatus() == MembershipStatus.ACTIVE
                ? Long.MAX_VALUE
                : Math.max(0, FREE_MONTHLY_LIMIT - usedViews);

        return new VideoAccessResponse(
                true,
                "Usage fetched",
                usedViews,
                remaining,
                user.getMembershipStatus().name()
        );
    }

    private void saveViewIfNew(Long userId, Long mealId, LocalDate month) {
        boolean alreadyExists = videoViewRepository
                .findByUserIdAndMealIdAndViewMonth(userId, mealId, month)
                .isPresent();

        if (!alreadyExists) {
            videoViewRepository.save(
                    VideoView.builder()
                            .userId(userId)
                            .mealId(mealId)
                            .viewMonth(month)
                            .build()
            );
        }
    }
}