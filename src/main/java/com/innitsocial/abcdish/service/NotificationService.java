package com.innitsocial.abcdish.service;

public interface NotificationService {

    void sendEmailOtp(String email, String otp, String purpose);

    void sendSmsOtp(String mobileNumber, String otp, String purpose);
}