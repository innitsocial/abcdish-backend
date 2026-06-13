package com.innitsocial.abcdish.notifications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@ConditionalOnProperty(
        prefix = "app.notifications.email",
        name = "provider",
        havingValue = "resend"
)
public class ResendEmailNotificationService implements NotificationService {

    private static final URI RESEND_EMAILS_URI = URI.create("https://api.resend.com/emails");

    @Value("${app.notifications.email.resend.api-key:}")
    private String apiKey;

    @Value("${app.notifications.email.from:no-reply@abcdish.com}")
    private String fromAddress;

    @Override
    public void sendEmailOtp(String email, String otp, String purpose) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Resend API key is not configured");
        }

        try {
            String body = """
                    {
                      "from": "%s",
                      "to": ["%s"],
                      "subject": "Your ABCDish verification code",
                      "text": "Your ABCDish code is %s.\\n\\nThis code expires in 10 minutes.",
                      "html": "<p>Your ABCDish code is <strong>%s</strong>.</p><p>This code expires in 10 minutes.</p>"
                    }
                    """.formatted(
                    jsonEscape(fromAddress),
                    jsonEscape(email),
                    jsonEscape(otp),
                    jsonEscape(otp)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(RESEND_EMAILS_URI)
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            HttpStatusCode statusCode = HttpStatusCode.valueOf(response.statusCode());
            if (statusCode.isError()) {
                throw new RuntimeException("Resend email failed: " + response.body());
            }
        } catch (IOException error) {
            throw new RuntimeException("Unable to send email through Resend", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email send interrupted", error);
        }
    }

    @Override
    public void sendSmsOtp(String mobileNumber, String otp, String purpose) {
        throw new RuntimeException("SMS OTP is not enabled");
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
