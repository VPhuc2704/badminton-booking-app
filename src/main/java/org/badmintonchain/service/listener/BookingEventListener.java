package org.badmintonchain.service.listener;

import org.badmintonchain.config.RabbitMQConfig;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.event.BookingCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
public class BookingEventListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private EmailService emailService;

    @EventListener(BookingCreatedEvent.class)
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        // 1. Gửi email xác nhận ngay
        emailService.sendBookingConfirmationEmail(
                event.getUserEmail(),
                "Xác nhận đặt sân #" + event.getBookingId(),
                "Bạn đã đặt sân " + event.getCourtName() +
                        " vào " + event.getBookingDate() +
                        " lúc " + event.getStartTime()
        );

        // 2. Tính thời điểm nhắc nhở
        LocalDateTime bookingDateTime = LocalDateTime.of(event.getBookingDate(), event.getStartTime());
        long delayMillis = Duration.between(LocalDateTime.now(), bookingDateTime.minusHours(24)).toMillis();

        if (delayMillis > 0) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.REMINDER_DELAY_QUEUE,
                    event,
                    message -> {
                        message.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                        return message;
                    }
            );
        }
    }

}
