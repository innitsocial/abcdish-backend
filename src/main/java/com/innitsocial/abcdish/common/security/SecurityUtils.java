package com.innitsocial.abcdish.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User is not authenticated");
        }

        return Long.valueOf(authentication.getPrincipal().toString());
    }

    public static Optional<Long> currentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(authentication.getPrincipal().toString()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
