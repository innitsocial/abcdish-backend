package com.innitsocial.abcdish.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
