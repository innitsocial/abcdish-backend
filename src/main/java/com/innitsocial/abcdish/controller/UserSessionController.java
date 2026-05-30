package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.config.SecurityUtils;
import com.innitsocial.abcdish.entity.UserSession;
import com.innitsocial.abcdish.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping
    public List<UserSession> getSessions() {
        return userSessionService.getActiveSessions(SecurityUtils.currentUserId());
    }

    @DeleteMapping("/{sessionId}")
    public void revokeSession(@PathVariable Long sessionId) {
        userSessionService.revokeSession(SecurityUtils.currentUserId(), sessionId);
    }
}