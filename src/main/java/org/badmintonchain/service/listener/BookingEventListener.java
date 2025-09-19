package org.badmintonchain.service.listener;

import org.badmintonchain.config.RabbitMQConfig;
import org.badmintonchain.model.enums.EmailType;
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

        // Gửi email xác nhận
        emailService.sendBookingEmail(event, EmailType.CONFIRMATION);

        // 2. Tính thời điểm nhắc nhở
        LocalDateTime bookingDateTime = LocalDateTime.of(event.getBookingDate(), event.getStartTime());
//        long delayMillis = Duration.between(LocalDateTime.now(), bookingDateTime.minusHours(24)).toMillis();
        long delayMillis = 30_000;
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
