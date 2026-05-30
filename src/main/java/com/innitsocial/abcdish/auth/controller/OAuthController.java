package com.innitsocial.abcdish.auth.controller;

import com.innitsocial.abcdish.auth.dto.OAuthLoginResponse;
import com.innitsocial.abcdish.auth.dto.OAuthProviderResponse;
import com.innitsocial.abcdish.auth.dto.OAuthStartResponse;
import com.innitsocial.abcdish.auth.oauth.OAuthProvider;
import com.innitsocial.abcdish.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    @GetMapping("/providers")
    public List<OAuthProviderResponse> getProviders() {
        return oauthService.getProviders();
    }

    @GetMapping("/{provider}/start")
    public OAuthStartResponse startLogin(@PathVariable OAuthProvider provider) {
        return oauthService.start(provider);
    }


    @PostMapping("/{provider}/login")
    public OAuthLoginResponse login(@PathVariable OAuthProvider provider) {
        return oauthService.login(provider);
    }

    @GetMapping("/google/start")
    public OAuthStartResponse startGoogleLogin() {
        return oauthService.start(OAuthProvider.GOOGLE);
    }

    @GetMapping("/apple/start")
    public OAuthStartResponse startAppleLogin() {
        return oauthService.start(OAuthProvider.APPLE);
    }
}
