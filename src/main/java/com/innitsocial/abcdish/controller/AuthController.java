package com.innitsocial.abcdish.controller;

import com.innitsocial.abcdish.dto.auth.*;
import com.innitsocial.abcdish.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.register(
                request,
                getDeviceName(httpRequest),
                getIpAddress(httpRequest)
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.login(
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
    public AuthResponse verifyEmailOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return authService.verifyEmailOtp(request);
    }

    @PostMapping("/mobile/request-otp")
    public void requestMobileOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestMobileOtp(request);
    }

    @PostMapping("/mobile/verify-otp")
    public AuthResponse verifyMobileOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return authService.verifyMobileOtp(request);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
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