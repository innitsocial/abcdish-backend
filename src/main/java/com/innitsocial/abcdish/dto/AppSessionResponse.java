package com.innitsocial.abcdish.dto;

public record AppSessionResponse(
        Long userId,
        String name,
        String email,
        String mobileNumber,
        boolean emailVerified,
        boolean mobileVerified,
        String role,
        String membershipStatus,
        int monthlyVideoViews,
        long remainingViews,
        Features features
) {
    public record Features(
            boolean canWatchUnlimitedVideos,
            boolean canUploadRecipes,
            boolean canAccessAdmin,
            boolean canManageMembership,
            boolean shouldShowUpgrade
    ) {
    }
}