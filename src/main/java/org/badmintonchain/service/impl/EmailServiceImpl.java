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
                  <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color:#f8f9fa; padding:30px;">
                    <div style="max-width:600px; margin:auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                
                      <!-- Header -->
                      <div style="background-color:#2ecc71; color:white; text-align:center; padding:20px 10px;">
                        <h2 style="margin:0;">%s</h2>
                      </div>
                
                      <!-- Content -->
                      <div style="padding:25px;">
                        <p style="font-size:16px;">Xin chào <b>%s</b>,</p>
                        <p style="font-size:15px; color:#333;">%s</p>
                
                        <table style="width:100%%; border-collapse:collapse; margin-top:15px; font-size:14px;">
                          <tr style="background-color:#f2f2f2;">
                            <td style="padding:10px; font-weight:bold; width:40%%;">Mã đặt sân</td>
                            <td style="padding:10px;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px; font-weight:bold;">Sân</td>
                            <td style="padding:10px;">%s <span style="color:#777;">(%s)</span></td>
                          </tr>
                          <tr style="background-color:#f2f2f2;">
                            <td style="padding:10px; font-weight:bold;">Thời gian</td>
                            <td style="padding:10px;">%s, từ %s đến %s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px; font-weight:bold;">Tổng tiền</td>
                            <td style="padding:10px; color:#27ae60; font-weight:bold;">%s VND</td>
                          </tr>
                          <tr style="background-color:#f2f2f2;">
                            <td style="padding:10px; font-weight:bold;">Số điện thoại</td>
                            <td style="padding:10px;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px; font-weight:bold;">Chi nhánh</td>
                            <td style="padding:10px;">
                                <b>%s</b><br>
                                %s<br>
                                <span style="color:#555;">Hotline: %s</span>
                            </td>
                          </tr>
                        </table>
                
                        <div style="margin-top:25px; text-align:center;">
                            <a href="#" style="background-color:#2ecc71; color:white; padding:10px 20px; border-radius:8px; text-decoration:none; font-weight:500;">Xem chi tiết đặt sân</a>
                        </div>
                
                        <p style="margin-top:25px; font-size:14px; color:#666; text-align:center;">
                          Cảm ơn bạn đã tin tưởng hệ thống của chúng tôi!<br>
                          Chuẩn bị sẵn sàng để có một buổi chơi tuyệt vời nhé 🎾
                        </p>
                      </div>
                
                      <!-- Footer -->
                      <div style="background-color:#f2f2f2; padding:15px; text-align:center; font-size:12px; color:#777;">
                        &copy; 2025 Court Booking System. All rights reserved.
                      </div>
                    </div>
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
                bookingEvent.getCustomerPhone(),
                bookingEvent.getBranchName(),
                bookingEvent.getBranchAddress(),
                bookingEvent.getBranchPhone()
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
