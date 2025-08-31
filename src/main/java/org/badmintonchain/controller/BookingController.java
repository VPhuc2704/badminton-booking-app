package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.BookingService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    // ---------- ADMIN ----------
    @GetMapping("/admin/bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getAllBookingsForAdmin(
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "1") int size,
                                                HttpServletRequest httpServletRequest) {
        PageResponse<BookingDTO> bookings = bookingService.getAllBookings(page, size);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Bookings retrieved successfully",
                        HttpStatus.OK.value(),
                        bookings,
                        httpServletRequest.getRequestURI()
                )
        );
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
}
