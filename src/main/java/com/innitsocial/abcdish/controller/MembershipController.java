package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.config.SecurityUtils;
import com.innitsocial.abcdish.dto.auth.ProfileResponse;
import com.innitsocial.abcdish.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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