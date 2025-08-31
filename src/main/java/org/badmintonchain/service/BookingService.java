package org.badmintonchain.service;

import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.enums.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDTO createBoking(BookingDTO booking,  Long userId);
    BookingDTO getBookingById(Long bookingId, Long userId);
    List<BookingDTO> getAllBookingByUserId(Long userId);
    PageResponse<BookingDTO> getAllBookings(int page, int size);
    BookingDTO updateBookingStatus(Long bookingId, BookingStatus newStatus );
}
