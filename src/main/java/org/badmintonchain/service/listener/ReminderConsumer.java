package org.badmintonchain.service.listener;

import org.badmintonchain.config.RabbitMQConfig;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.EmailType;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.service.BookingService;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.event.BookingCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReminderConsumer {
    @Autowired
    private EmailService emailService;
    @Autowired
    private BookingRepository bookingRepository;

    @RabbitListener(queues = RabbitMQConfig.REMINDER_QUEUE)
    public void handleReminder(BookingCreatedEvent event) {
        BookingsEntity booking = bookingRepository.findById(event.getBookingId())
                .orElse(null);

        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            // Booking đã hủy → không gửi email
            System.out.println("Reminder skipped for cancelled booking: "+ event.getBookingCode());
            return;
        }
        // Gửi email nhắc lịch
        emailService.sendBookingEmail(event, EmailType.REMINDER);

    }
}
