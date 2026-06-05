package com.innitsocial.abcdish.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(
        prefix = "app.notifications.email",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class ConsoleNotificationService implements NotificationService {

    @Override
    public void sendEmailOtp(String email, String otp, String purpose) {
        log.info("ABCDish EMAIL OTP destination={} purpose={} otp={}", email, purpose, otp);
    }

    @Override
    public void sendSmsOtp(String mobileNumber, String otp, String purpose) {
        log.info("ABCDish SMS OTP destination={} purpose={} otp={}", mobileNumber, purpose, otp);
    }
}
