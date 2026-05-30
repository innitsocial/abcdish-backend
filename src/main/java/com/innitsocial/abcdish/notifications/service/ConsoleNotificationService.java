package com.innitsocial.abcdish.notifications.service;

import org.springframework.stereotype.Service;

@Service
public class ConsoleNotificationService implements NotificationService {

    @Override
    public void sendEmailOtp(String email, String otp, String purpose) {
        System.out.println("ABCDish EMAIL OTP");
        System.out.println("To: " + email);
        System.out.println("Purpose: " + purpose);
        System.out.println("OTP: " + otp);
    }

    @Override
    public void sendSmsOtp(String mobileNumber, String otp, String purpose) {
        System.out.println("ABCDish SMS OTP");
        System.out.println("To: " + mobileNumber);
        System.out.println("Purpose: " + purpose);
        System.out.println("OTP: " + otp);
    }
}