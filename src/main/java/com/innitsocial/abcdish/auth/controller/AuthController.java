package com.innitsocial.abcdish.auth.controller;

import com.innitsocial.abcdish.auth.dto.*;
import com.innitsocial.abcdish.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/email/request-otp")
    public void requestRegisterEmailOtp(@Valid @RequestBody RegisterEmailOtpRequest request) {
        authService.requestRegisterEmailOtp(request);
    }

    @PostMapping("/register/email/verify-otp")
    public AuthResponse verifyRegisterEmailOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.verifyRegisterEmailOtp(
                request,
                getDeviceName(httpRequest),
                getIpAddress(httpRequest)
        );
    }

    @PostMapping("/email/request-otp")
    public void requestEmailOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestEmailOtp(request);
    }

    @PostMapping("/email/verify-otp")
    public AuthResponse verifyEmailOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.verifyEmailOtp(
                request,
                getDeviceName(httpRequest),
                getIpAddress(httpRequest)
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
    }


    private String getIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String getDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? "Unknown device" : userAgent;
    }
}
