package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.config.SecurityUtils;
import com.innitsocial.abcdish.dto.auth.CommunicationPreferencesRequest;
import com.innitsocial.abcdish.dto.auth.ProfileResponse;
import com.innitsocial.abcdish.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.innitsocial.abcdish.dto.auth.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final AuthService authService;

    @GetMapping("/me")
    public ProfileResponse getMyProfile() {
        return authService.getProfile(SecurityUtils.currentUserId());
    }

    @PutMapping("/me/communication-preferences")
    public ProfileResponse updateMyCommunicationPreferences(
            @RequestBody CommunicationPreferencesRequest request
    ) {
        return authService.updateCommunicationPreferences(
                SecurityUtils.currentUserId(),
                request
        );
    }


    @PostMapping("/me/link-email/request")
    public void requestLinkEmail(@Valid @RequestBody LinkEmailRequest request) {
        authService.requestLinkEmail(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/me/link-email/verify")
    public ProfileResponse verifyLinkEmail(@Valid @RequestBody VerifyLinkEmailRequest request) {
        return authService.verifyLinkEmail(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/me/link-mobile/request")
    public void requestLinkMobile(@Valid @RequestBody LinkMobileRequest request) {
        authService.requestLinkMobile(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/me/link-mobile/verify")
    public ProfileResponse verifyLinkMobile(@Valid @RequestBody VerifyLinkMobileRequest request) {
        return authService.verifyLinkMobile(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/me/set-password")
    public ProfileResponse setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return authService.setPassword(SecurityUtils.currentUserId(), request);
    }
}