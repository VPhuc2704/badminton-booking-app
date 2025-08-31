package org.badmintonchain.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private Long bookingId;
    private String bookingCode;
    private String userEmail;
    private String fullName;
    private String courtName;
    private String courtType;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalAmount;
    private String customerPhone;
}
