package org.badmintonchain.service;

import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.dto.requests.AdminCreateBookingDTO;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {
    BookingDTO createBoking(BookingDTO booking,  Long userId);
    BookingDTO getBookingById(Long bookingId, Long userId);
    List<BookingDTO> getAllBookingByUserId(Long userId);
    BookingDTO cancelBooking(Long bookingId, Long userId);
    PageResponse<BookingDTO> getAllBookings(int page, int size,  Integer year, Integer month, Integer week ,LocalDate day);
    BookingDTO updateBookingStatus(Long bookingId, BookingStatus newStatus );
    void deleteBooking(Long bookingId);

    BookingDTO processPayment(Long bookingId, PaymentMethod method,String adminName);
    BookingDTO getBookingByIdForAdmin(Long bookingId);

    BookingDTO createBookingByAdmin(AdminCreateBookingDTO bookingRequest);

    boolean isCourtAvailable(Long courtId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
