package org.badmintonchain.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.enums.EmailType;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.event.BookingCreatedEvent;
import org.springframework.mail.SimpleMailMessage;
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

    @Override
    public void sendBookingEmail(BookingCreatedEvent bookingEvent, EmailType type) {
        String to = bookingEvent.getUserEmail();

        String subject = switch (type){
            case PENDING        -> "Đặt sân thành công #" + bookingEvent.getBookingCode();
            case CONFIRMATION   -> "Xác nhận đặt sân #" + bookingEvent.getBookingCode();
            case REMINDER       -> "Nhắc nhở lịch đặt sân #" + bookingEvent.getBookingCode();
            case CANCELLED      -> "Hủy đặt sân #" + bookingEvent.getBookingCode();
        };

        String header = switch (type){
            case PENDING      -> "Đặt sân thành công (chờ xác nhận)";
            case CONFIRMATION ->  "Xác nhận đặt sân thành công";
            case REMINDER     -> "Nhắc nhở lịch đặt sân";
            case CANCELLED    -> "Lịch đặt sân của bạn đã bị hủy.";
        };

        String messageLine = switch (type) {
            case PENDING      -> "Bạn đã đặt sân thành công, vui lòng chờ admin xác nhận.";
            case CONFIRMATION -> "Bạn đã đặt sân thành công. Thông tin chi tiết như sau:";
            case REMINDER     -> "Đây là lời nhắc: bạn có lịch đặt sân trong vòng 24 giờ tới.";
            case CANCELLED    -> "Lịch đặt sân của bạn đã bị hủy.";
        };

        String body = """
            <html>
              <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2 style="color:#2ecc71;">%s</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>%s</p>

                <table border="1" cellpadding="8" cellspacing="0" style="border-collapse: collapse; width: 100%%;">
                  <tr>
                    <td><b>Mã đặt sân</b></td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td><b>Sân</b></td>
                    <td>%s (%s)</td>
                  </tr>
                  <tr>
                    <td><b>Thời gian</b></td>
                    <td>%s, từ %s đến %s</td>
                  </tr>
                  <tr>
                    <td><b>Tổng tiền</b></td>
                    <td>%s VND</td>
                  </tr>
                  <tr>
                    <td><b>Số điện thoại</b></td>
                    <td>%s</td>
                  </tr>
                </table>

                <p style="margin-top:20px;">Chuẩn bị sẵn sàng để có một buổi chơi tuyệt vời nhé!</p>
                <p>Hẹn gặp bạn tại sân!</p>
              </body>
            </html>
            """.formatted(
                header,
                bookingEvent.getFullName(),
                messageLine,
                bookingEvent.getBookingCode(),
                bookingEvent.getCourtName(),
                bookingEvent.getCourtType(),
                bookingEvent.getBookingDate(),
                bookingEvent.getStartTime(),
                bookingEvent.getEndTime(),
                bookingEvent.getTotalAmount().toPlainString(),
                bookingEvent.getCustomerPhone()
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

            System.out.println("Email " + type + " đã gửi tới " + to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendQuotationEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
