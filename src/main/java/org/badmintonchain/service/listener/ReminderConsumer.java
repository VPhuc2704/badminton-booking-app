package org.badmintonchain.service.listener;

import org.badmintonchain.config.RabbitMQConfig;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.event.BookingCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReminderConsumer {
    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.REMINDER_QUEUE)
    public void handleReminder(BookingCreatedEvent event) {
        emailService.sendBookingConfirmationEmail(
                event.getUserEmail(),
                "Nhắc nhở lịch đặt sân",
                "Đây là lời nhắc: bạn có lịch tại " + event.getCourtName() +
                        " vào " + event.getBookingDate() + " lúc " + event.getStartTime()
        );
    }
}
