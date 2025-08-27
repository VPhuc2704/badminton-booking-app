package org.badmintonchain.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendBookingConfirmationEmail(String to, String subject, String content);
}
