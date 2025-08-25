package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.BookingService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/api")
public class BookingController {
    @Autowired
    private BookingService bookingService;

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


}
