package org.badmintonchain.model.dto;

import lombok.Data;
import org.badmintonchain.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private BookingStatus status;
    private BigDecimal totalAmount;
}
