package com.innitsocial.abcdish.auth.service;

import com.innitsocial.abcdish.auth.entity.AppUser;
import com.innitsocial.abcdish.auth.entity.RefreshToken;
import com.innitsocial.abcdish.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public String createRefreshToken(AppUser user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revoke(String token) {
        refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }
}