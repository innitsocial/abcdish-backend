package com.innitsocial.abcdish.notifications.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.notifications.email",
        name = "provider",
        havingValue = "smtp"
)
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.from:no-reply@abcdish.com}")
    private String fromAddress;

    @Override
    public void sendEmailOtp(String email, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Your ABCDish verification code");
        message.setText("""
                Your ABCDish code is %s.

                This code expires in 10 minutes.
                """.formatted(otp));

        mailSender.send(message);
    }

    @Override
    public void sendSmsOtp(String mobileNumber, String otp, String purpose) {
        throw new RuntimeException("SMS OTP is not enabled");
    }
}
