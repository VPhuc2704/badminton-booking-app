package org.badmintonchain.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.badmintonchain.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + token;

        String subject = "Email Verification";
        String content = "<p>Xin chào,</p>"
                + "<p>Cảm ơn bạn đã đăng ký. Vui lòng nhấn vào link bên dưới để xác thực tài khoản:</p>"
                + "<p><a href=\"" + verifyLink + "\">Xác thực tài khoản</a></p>"
                + "<br>"
                + "<p>Link sẽ hết hạn sau 24h.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
