package com.example.sclms_notification.service;

import com.example.sclms_notification.dto.NotificationEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.support}")
    private String supportEmail;

    public void sendSchoolRegisteredEmail(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        String schoolEmail = (String) payload.getOrDefault("schoolEmail", "Unknown email");
        String schoolId = String.valueOf(payload.getOrDefault("schoolId", ""));

        String subject = "New School Registration - Approval Required";
        String body = """
                <html>
                <body>
                <h2>New School Registration Request</h2>
                <p>A new school has registered and is awaiting your approval.</p>
                <table>
                    <tr><td><strong>School Name:</strong></td><td>%s</td></tr>
                    <tr><td><strong>School ID:</strong></td><td>%s</td></tr>
                </table>
                <p>Please log in to the Government Portal to review and approve this registration.</p>
                <br>
                <p>Regards,<br>School Management System</p>
                </body>
                </html>
                """.formatted(schoolEmail, schoolId);

        // Send to government email (recipient holds the gov email)
        sendHtmlEmail(event.getRecipient(), subject, body);
        log.info("School registration notification sent to government for school: {}", schoolEmail);
    }

    public void sendSchoolApprovedEmail(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        String schoolName = (String) payload.getOrDefault("schoolName", "School");

        String subject = "Your School Account Has Been Approved!";
        String body = """
                <html>
                <body>
                <h2>Congratulations, %s!</h2>
                <p>Your school account has been <strong>approved</strong> by the Government.</p>
                <p>You can now log in to the School Portal and start using all available features.</p>
                <br>
                <p>If you have any questions, please contact us at <a href="mailto:%s">%s</a>.</p>
                <p>Regards,<br>School Management System</p>
                </body>
                </html>
                """.formatted(schoolName, supportEmail, supportEmail);

        sendHtmlEmail(event.getRecipient(), subject, body);
        log.info("School approval notification sent to: {}", event.getRecipient());
    }

    public void sendPasswordResetEmail(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        String otp = String.valueOf(payload.getOrDefault("otp", ""));
        int expiryMinutes = payload.containsKey("expiryMinutes")
                ? ((Number) payload.get("expiryMinutes")).intValue()
                : 5;

        String subject = "Password Reset OTP";
        String body = """
                <html>
                <body>
                <h2>Password Reset Request</h2>
                <p>You have requested to reset your password. Use the OTP below to proceed:</p>
                <h1 style="text-align:center; color:#2c3e50; letter-spacing:8px;">%s</h1>
                <p>This OTP is valid for <strong>%d minutes</strong>. Do not share it with anyone.</p>
                <p>If you did not request this, please ignore this email.</p>
                <br>
                <p>Regards,<br>School Management System</p>
                </body>
                </html>
                """.formatted(otp, expiryMinutes);

        sendHtmlEmail(event.getRecipient(), subject, body);
        log.info("Password reset OTP sent to: {}", event.getRecipient());
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
