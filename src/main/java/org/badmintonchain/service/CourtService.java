package org.badmintonchain.service;

import org.badmintonchain.model.dto.AvailabilitySlotDTO;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CourtService {
    PageResponse<CourtDTO> getAllCourts(int page, int size);
    PageResponse<CourtDTO> getAllActiveCourts(int page, int size);
    CourtDTO getCourtById(Long id);
    CourtDTO getCourtIfAvailable(Long id);
    CourtDTO createCourt(CourtDTO court);
    CourtDTO updateCourt(Long id, CourtDTO court);
    void deleteCourtById(Long id);

    boolean isCourtAvailable(Long courtId, LocalDate date, LocalTime startTime, LocalTime endTime);
    List<AvailabilitySlotDTO> getAvailableSlots(Long courtId, LocalDate date);
}
