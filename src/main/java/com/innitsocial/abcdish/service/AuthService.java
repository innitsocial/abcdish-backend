package com.innitsocial.abcdish.service;

import com.innitsocial.abcdish.dto.auth.*;
import com.innitsocial.abcdish.entity.*;
import com.innitsocial.abcdish.repository.AppUserRepository;
import com.innitsocial.abcdish.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionService userSessionService;
    private final NotificationService notificationService;


    public AuthResponse register(
            RegisterRequest request,
            String deviceName,
            String ipAddress
    ) {
        if ((request.email() == null || request.email().isBlank())
                && (request.mobileNumber() == null || request.mobileNumber().isBlank())) {
            throw new RuntimeException("Email or mobile number is required");
        }

        if (request.email() != null && !request.email().isBlank()
                && appUserRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()
                && appUserRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new RuntimeException("Mobile number already registered");
        }

        String passwordHash = null;

        if (request.password() != null && !request.password().isBlank()) {
            passwordHash = passwordEncoder.encode(request.password());
        }

        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email())
                .mobileNumber(request.mobileNumber())
                .passwordHash(passwordHash)
                .emailVerified(false)
                .mobileVerified(false)
                .membershipStatus(MembershipStatus.FREE)
                .monthlyVideoViews(0)
                .role(UserRole.USER)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        userSessionService.createSession(savedUser.getId(), deviceName, ipAddress);
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(
            LoginRequest request,
            String deviceName,
            String ipAddress
    ) {

        AppUser user = findByIdentifier(request.identifier());

        if (isEmail(request.identifier())) {
            if (user.getPasswordHash() == null) {
                throw new RuntimeException("Password login not enabled for this account");
            }

            if (request.password() == null || request.password().isBlank()) {
                throw new RuntimeException("Password is required");
            }

            boolean validPassword = passwordEncoder.matches(
                    request.password(),
                    user.getPasswordHash()
            );

            if (!validPassword) {
                throw new RuntimeException("Invalid login details");
            }
        } else {
            throw new RuntimeException("Use OTP login for mobile number");
        }

        userSessionService.createSession(user.getId(), deviceName, ipAddress);
        return toAuthResponse(user);
    }

    public void requestEmailOtp(OtpRequest request) {
        if (!isEmail(request.destination())) {
            throw new RuntimeException("Valid email address is required");
        }

        createOtp(request.destination(), OtpType.EMAIL, OtpPurpose.LOGIN);

        // TODO: send email here using SES/SendGrid later.
        System.out.println("ABCDish email OTP sent to: " + request.destination());
    }

    public AuthResponse verifyEmailOtp(OtpVerifyRequest request) {
        OtpCode otpCode = verifyOtpCode(
                request.destination(),
                request.otp(),
                OtpType.EMAIL,
                OtpPurpose.LOGIN
        );

        AppUser user = appUserRepository.findByEmail(request.destination())
                .orElseGet(() -> appUserRepository.save(
                        AppUser.builder()
                                .email(request.destination())
                                .emailVerified(true)
                                .membershipStatus(MembershipStatus.FREE)
                                .monthlyVideoViews(0)
                                .build()
                ));

        user.setEmailVerified(true);
        otpCode.setUsed(true);

        otpCodeRepository.save(otpCode);
        AppUser savedUser = appUserRepository.save(user);

        return toAuthResponse(savedUser);
    }

    public void requestMobileOtp(OtpRequest request) {
        if (request.destination() == null || request.destination().isBlank()) {
            throw new RuntimeException("Mobile number is required");
        }

        createOtp(request.destination(), OtpType.MOBILE, OtpPurpose.LOGIN);

        // TODO: send SMS here using AWS SNS/Twilio later.
        System.out.println("ABCDish mobile OTP sent to: " + request.destination());
    }

    public AuthResponse verifyMobileOtp(OtpVerifyRequest request) {

        OtpCode otpCode = verifyOtpCode(
                request.destination(),
                request.otp(),
                OtpType.MOBILE,
                OtpPurpose.LOGIN
        );

        AppUser user = appUserRepository.findByMobileNumber(request.destination())
                .orElseGet(() -> appUserRepository.save(
                        AppUser.builder()
                                .mobileNumber(request.destination())
                                .mobileVerified(true)
                                .membershipStatus(MembershipStatus.FREE)
                                .monthlyVideoViews(0)
                                .build()
                ));

        user.setMobileVerified(true);
        otpCode.setUsed(true);

        otpCodeRepository.save(otpCode);
        AppUser savedUser = appUserRepository.save(user);

        return toAuthResponse(savedUser);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("No account found for this email"));

        if (user.getPasswordHash() == null) {
            throw new RuntimeException("Password login is not enabled for this account");
        }

        createOtp(request.email(), OtpType.EMAIL, OtpPurpose.RESET_PASSWORD);
    }

    public void resetPassword(ResetPasswordRequest request) {
        OtpCode otpCode = verifyOtpCode(
                request.email(),
                request.otp(),
                OtpType.EMAIL,
                OtpPurpose.RESET_PASSWORD
        );

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("No account found for this email"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        otpCode.setUsed(true);

        otpCodeRepository.save(otpCode);
        appUserRepository.save(user);
    }

    public ProfileResponse getProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toProfileResponse(user);
    }

    public ProfileResponse updateCommunicationPreferences(
            Long userId,
            CommunicationPreferencesRequest request
    ) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setNewsletterSubscribed(request.newsletterSubscribed());
        user.setEmailMarketingEnabled(request.emailMarketingEnabled());
        user.setSmsMarketingEnabled(request.smsMarketingEnabled());
        user.setPushNotificationsEnabled(request.pushNotificationsEnabled());

        AppUser savedUser = appUserRepository.save(user);

        return toProfileResponse(savedUser);
    }

    public ProfileResponse getMembershipStatus(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toProfileResponse(user);
    }

    public ProfileResponse upgradeMembershipForTesting(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMembershipStatus(MembershipStatus.ACTIVE);

        return toProfileResponse(appUserRepository.save(user));
    }

    public void requestLinkEmail(Long userId, LinkEmailRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already linked to another account");
        }

        appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createOtp(request.email(), OtpType.EMAIL, OtpPurpose.VERIFY_EMAIL);
    }

    public ProfileResponse verifyLinkEmail(Long userId, VerifyLinkEmailRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (appUserRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already linked to another account");
        }

        OtpCode otpCode = verifyOtpCode(
                request.email(),
                request.otp(),
                OtpType.EMAIL,
                OtpPurpose.VERIFY_EMAIL
        );

        user.setEmail(request.email());
        user.setEmailVerified(true);
        otpCode.setUsed(true);

        otpCodeRepository.save(otpCode);
        return toProfileResponse(appUserRepository.save(user));
    }

    public void requestLinkMobile(Long userId, LinkMobileRequest request) {
        if (appUserRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new RuntimeException("Mobile number is already linked to another account");
        }

        appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createOtp(request.mobileNumber(), OtpType.MOBILE, OtpPurpose.VERIFY_MOBILE);
    }

    public ProfileResponse verifyLinkMobile(Long userId, VerifyLinkMobileRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (appUserRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new RuntimeException("Mobile number is already linked to another account");
        }

        OtpCode otpCode = verifyOtpCode(
                request.mobileNumber(),
                request.otp(),
                OtpType.MOBILE,
                OtpPurpose.VERIFY_MOBILE
        );

        user.setMobileNumber(request.mobileNumber());
        user.setMobileVerified(true);
        otpCode.setUsed(true);

        otpCodeRepository.save(otpCode);
        return toProfileResponse(appUserRepository.save(user));
    }

    public ProfileResponse setPassword(Long userId, SetPasswordRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        return toProfileResponse(appUserRepository.save(user));
    }

    private ProfileResponse toProfileResponse(AppUser user) {
        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                user.getMembershipStatus().name(),
                user.getMonthlyVideoViews() == null ? 0 : user.getMonthlyVideoViews(),
                user.isNewsletterSubscribed(),
                user.isEmailMarketingEnabled(),
                user.isSmsMarketingEnabled(),
                user.isPushNotificationsEnabled()
        );
    }

    private void createOtp(String destination, OtpType type, OtpPurpose purpose) {
        enforceOtpRateLimit(destination, type);

        String otp = generateOtp();

        OtpCode otpCode = OtpCode.builder()
                .destination(destination)
                .otp(otp)
                .type(type)
                .purpose(purpose)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpCodeRepository.save(otpCode);

        if (type == OtpType.EMAIL) {
            notificationService.sendEmailOtp(destination, otp, purpose.name());
        } else {
            notificationService.sendSmsOtp(destination, otp, purpose.name());
        }
    }

    private void enforceOtpRateLimit(String destination, OtpType type) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        long recentOtpCount = otpCodeRepository.countByDestinationAndTypeAndCreatedAtAfter(
                destination,
                type,
                tenMinutesAgo
        );

        if (recentOtpCount >= 3) {
            String msg = "Too many OTP requests.";
            throw new RuntimeException(msg);
        }
    }

    private OtpCode verifyOtpCode(
            String destination,
            String otp,
            OtpType type,
            OtpPurpose purpose
    ) {
        OtpCode otpCode = otpCodeRepository
                .findTopByDestinationAndOtpAndTypeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        destination,
                        otp,
                        type,
                        purpose
                )
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        return otpCode;
    }

    private AppUser findByIdentifier(String identifier) {
        if (isEmail(identifier)) {
            return appUserRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Invalid login details"));
        }

        return appUserRepository.findByMobileNumber(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid login details"));
    }

    private String generateOtp() {
        int number = new Random().nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshTokenService.createRefreshToken(user),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                user.getMembershipStatus().name()
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        var refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());

        AppUser user = appUserRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                jwtService.generateToken(user),
                request.refreshToken(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                user.getMembershipStatus().name()
        );
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private boolean isEmail(String value) {
        return value != null && value.contains("@");
    }
}