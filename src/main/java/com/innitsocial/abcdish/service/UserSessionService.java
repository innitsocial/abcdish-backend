package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.entity.UserSession;
import com.innitsocial.abcdish.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    public UserSession createSession(Long userId, String deviceName, String ipAddress) {
        UserSession session = UserSession.builder()
                .userId(userId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .active(true)
                .lastSeenAt(LocalDateTime.now())
                .build();

        return userSessionRepository.save(session);
    }

    public List<UserSession> getActiveSessions(Long userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    public void updateLastSeen(Long userId) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndActiveTrue(userId);

        if (sessions.isEmpty()) {
            return;
        }

        UserSession latestSession = sessions.get(0);
        latestSession.setLastSeenAt(LocalDateTime.now());
        userSessionRepository.save(latestSession);
    }

    public void revokeSession(Long userId, Long sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("You cannot revoke this session");
        }

        session.setActive(false);
        userSessionRepository.save(session);
    }
}