package com.innitsocial.abcdish.auth.controller;

import com.innitsocial.abcdish.auth.dto.OAuthStartResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
@CrossOrigin(origins = "*")
public class OAuthController {

    @GetMapping("/google/start")
    public OAuthStartResponse startGoogleLogin() {
        return new OAuthStartResponse(
                "GOOGLE",
                "/oauth2/authorization/google",
                "Google OAuth will be enabled later."
        );
    }

    @GetMapping("/apple/start")
    public OAuthStartResponse startAppleLogin() {
        return new OAuthStartResponse(
                "APPLE",
                "/oauth2/authorization/apple",
                "Apple OAuth will be enabled later."
        );
    }
}