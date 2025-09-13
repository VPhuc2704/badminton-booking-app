package org.badmintonchain.model.dto.requests;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdminCreateBookingDTO {
    private Long courtId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String email;
    private String numberPhone;
}
