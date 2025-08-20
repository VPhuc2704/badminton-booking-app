package org.badmintonchain.service;

import org.badmintonchain.model.dto.CourtDTO;

import java.util.List;

public interface CourtService {
    List<CourtDTO> getAllCourts();
    List<CourtDTO> getAllActiveCourts();
    CourtDTO getCourtById(Long id);
    CourtDTO createCourt(CourtDTO court);
    CourtDTO updateCourt(Long id, CourtDTO court);
    void deleteCourtById(Long id);
}
