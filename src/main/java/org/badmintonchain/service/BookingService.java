package org.badmintonchain.service;

import org.badmintonchain.model.dto.BookingDTO;

import java.util.List;

public interface BookingService {
    BookingDTO createBoking(BookingDTO booking,  Long userId);
    BookingDTO getBookingById(Long bookingId, Long userId);
    List<BookingDTO> getAllBookingByUserId(Long userId);

}
