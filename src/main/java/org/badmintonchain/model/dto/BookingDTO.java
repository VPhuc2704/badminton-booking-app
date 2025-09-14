package org.badmintonchain.model.dto;

import lombok.Data;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.PaymentMethod;
import org.badmintonchain.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingDTO {
    private Long id;
    private String bookingCode;
    private CourtDTO court;
    private CustomerDTO customer;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double duration;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
