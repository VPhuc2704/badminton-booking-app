package org.badmintonchain.service;

import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.enums.EmailType;
import org.badmintonchain.service.event.BookingCreatedEvent;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendBookingEmail(BookingCreatedEvent bookingEvent, EmailType type);
}
