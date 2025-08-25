package org.badmintonchain.service;

import org.badmintonchain.model.dto.BookingDTO;

public interface BookingService {
    BookingDTO createBoking(BookingDTO booking,  Long userId);
}
