package com.yasirkhan.auth.services.implementations;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // CHANGED: was hardcoded as "your-official-email@gmail.com" in the method body, separate
    // from spring.mail.username in application.properties - meaning the actual sending account
    // and the "From" header could silently drift apart. Now sourced from the same property.
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("WTMS - Password Reset Code");
            helper.setText(buildResetEmailHtml(resetToken), true);

            mailSender.send(message);

            log.info("Password reset HTML OTP successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send HTML password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    // CHANGED: converted the string-concatenation HTML build-up into a text block - same
    // output, easier to read/maintain.
    private String buildResetEmailHtml(String resetToken) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
                <h2 style="color: #333; text-align: center;">WTMS Password Reset</h2>
                <p>Hello,</p>
                <p>We received a request to reset the password for your account. Please enter the following 6-digit code in the WTMS app to securely set a new password:</p>
                <div style="text-align: center; margin: 30px 0;">
                <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #28a745; background: #f4f4f4; padding: 15px 30px; border-radius: 8px; border: 2px dashed #28a745; display: inline-block;">%s</span>
                </div>
                <p style="text-align: center; color: #d9534f; font-size: 0.9em; font-weight: bold;">This code will expire in exactly 15 minutes.</p>
                <p>If you did not request a password reset, please ignore this email or contact your administrator immediately.</p>
                <br>
                <p>Regards,<br><strong>WTMS Support Team</strong></p>
                </div>
                """.formatted(resetToken);
    }
}