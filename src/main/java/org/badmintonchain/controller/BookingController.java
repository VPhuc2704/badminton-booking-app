package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.dto.requests.AdminCreateBookingDTO;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.PaymentMethod;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.BookingService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping(value = "/api")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    // ---------- USER ----------
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                                @RequestBody BookingDTO request,
                                                                 HttpServletRequest httpServletRequest) {
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            ApiResponse<BookingDTO> errorResponse = new ApiResponse<>(
                    "End time must be after start time",
                    HttpStatus.BAD_REQUEST.value(),
                    null,
                    httpServletRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        BookingDTO booking = bookingService.createBoking(request, currentUser.getUser().getId());

        ApiResponse<BookingDTO> response = new ApiResponse<>(
                "Booking created successfully",
                HttpStatus.OK.value(),
                booking,
                httpServletRequest.getRequestURI()   // "/bookings"
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                                 @PathVariable("id") Long bookingId,
                                                                 HttpServletRequest httpServletRequest) {
        BookingDTO booking = bookingService.getBookingById(bookingId, currentUser.getUser().getId());

        ApiResponse<BookingDTO> response = new ApiResponse<>(
                "Booking retrieved  successfully",
                HttpStatus.OK.value(),
                booking,
                httpServletRequest.getRequestURI()   // "/bookings"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookingsByUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpServletRequest
    ) {
        List<BookingDTO> bookings = bookingService.getAllBookingByUserId(currentUser.getUser().getId());

        ApiResponse<List<BookingDTO>> response = new ApiResponse<>(
                "Bookings retrieved successfully",
                HttpStatus.OK.value(),
                bookings,
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("id") Long bookingId,
            HttpServletRequest httpServletRequest) {

        BookingDTO booking = bookingService.cancelBooking(bookingId, currentUser.getUser().getId());

        return ResponseEntity.ok(
                new ApiResponse<>("Booking cancelled successfully", HttpStatus.OK.value(),
                        booking, httpServletRequest.getRequestURI()));
    }


    // ---------- ADMIN ----------
    @GetMapping("/admin/bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getAllBookingsForAdmin(
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "1") int size,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam(required = false) Integer month,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
                                                HttpServletRequest httpServletRequest) {
        PageResponse<BookingDTO> bookings = bookingService.getAllBookings(page, size, year, month, day);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Bookings retrieved successfully",
                        HttpStatus.OK.value(),
                        bookings,
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @GetMapping("/admin/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByIdForAdmin(
            @PathVariable("id") Long bookingId,
            HttpServletRequest httpServletRequest) {

        BookingDTO booking = bookingService.getBookingByIdForAdmin(bookingId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Booking retrieved successfully",
                        HttpStatus.OK.value(),
                        booking,
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @PostMapping("/admin/bookings")
    public ResponseEntity<ApiResponse<BookingDTO>> createBookingForUser(@RequestBody AdminCreateBookingDTO request,
                                                                        HttpServletRequest httpServletRequest) {

        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            ApiResponse<BookingDTO> errorResponse = new ApiResponse<>(
                    "End time must be after start time",
                    HttpStatus.BAD_REQUEST.value(),
                    null,
                    httpServletRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        BookingDTO createdBooking = bookingService.createBookingByAdmin(request);

        ApiResponse<BookingDTO> response = new ApiResponse<>(
                "Booking created successfully",
                HttpStatus.OK.value(),
                createdBooking,
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping("/admin/bookings/{id}/status")
    public ResponseEntity<ApiResponse<BookingDTO>> confirmBooking(
            @PathVariable("id") Long bookingId,
            @RequestParam BookingStatus newStatus,
            HttpServletRequest httpServletRequest) {
        BookingDTO booking = bookingService.updateBookingStatus(bookingId, newStatus);

        return ResponseEntity.ok(
                new ApiResponse<>("Booking confirmed successfully", HttpStatus.OK.value(),
                        booking, httpServletRequest.getRequestURI()));
    }

    @DeleteMapping("/admin/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @PathVariable("id") Long bookingId,
            HttpServletRequest httpServletRequest) {

        bookingService.deleteBooking(bookingId);

        return ResponseEntity.ok(
                new ApiResponse<>("Booking deleted successfully", HttpStatus.OK.value(),
                        null, httpServletRequest.getRequestURI()));
    }


    @PostMapping("/admin/bookings/{bookingId}/pay")
    public ResponseEntity<ApiResponse<BookingDTO>> payBooking(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                              @PathVariable("bookingId") Long bookingId,
                                                              @RequestParam PaymentMethod method,
                                                              HttpServletRequest httpServletRequest) {


        BookingDTO dto = bookingService.processPayment(bookingId, method, currentUser.getUser().getFullName() );
        return ResponseEntity.ok(
                new ApiResponse<>(
                "Payment processed successfully",
                HttpStatus.OK.value(),
                dto,
                httpServletRequest.getRequestURI()
        ));
    }
}
