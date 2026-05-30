package com.innitsocial.abcdish.membership.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.users.dto.ProfileResponse;
import com.innitsocial.abcdish.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final AuthService authService;

    @GetMapping("/me")
    public ProfileResponse getMyMembership() {
        return authService.getMembershipStatus(SecurityUtils.currentUserId());
    }

    @PostMapping("/me/upgrade-test")
    public ProfileResponse upgradeMeForTesting() {
        return authService.upgradeMembershipForTesting(SecurityUtils.currentUserId());
    }
}