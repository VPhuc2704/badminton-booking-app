package org.badmintonchain.service.impl;

import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.mapper.CourtMapper;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourtServiceImpl implements CourtService {
    @Autowired
    private CourtRepository courtRepository;

    // Admin: lấy tất cả sân
    @Override
    public List<CourtDTO> getAllCourts() {
        List<CourtDTO> courtDTOList = new ArrayList<>();
        courtDTOList = courtRepository.findAll()
                .stream()
                .map(CourtMapper::toCourtDTO)
                .toList();
        return courtDTOList;
    }

    // User: chỉ lấy sân active
    @Override
    public List<CourtDTO> getAllActiveCourts() {
        return courtRepository.findByIsActiveTrue()
                .stream()
                .map(CourtMapper::toCourtDTO)
                .toList();
    }

    @Override
    public CourtDTO getCourtById(Long id) {
        CourtEntity court = courtRepository.findById(id).get();
        CourtDTO courtDTO = CourtMapper.toCourtDTO(court);
        return courtDTO;
    }

    @Override
    public CourtDTO createCourt(CourtDTO court) {
        CourtEntity courtEntity = CourtMapper.toCourtEntity(court);
        CourtEntity saved = courtRepository.save(courtEntity);
        return CourtMapper.toCourtDTO(saved);
    }

    @Override
    public CourtDTO updateCourt(Long id, CourtDTO courtDTO) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        if (courtDTO.getCourtName() != null) {
            court.setCourtName(courtDTO.getCourtName());
        }
        if (courtDTO.getCourtType() != null) {
            court.setCourtType(courtDTO.getCourtType());
        }
        if (courtDTO.getHourlyRate() != null) {
            court.setHourlyRate(courtDTO.getHourlyRate());
        }
        if (courtDTO.getDescription() != null) {
            court.setDescription(courtDTO.getDescription());
        }
        if (courtDTO.getImages() != null) {
            court.setImages(courtDTO.getImages());
        }
        if (courtDTO.getIsActive() != null) {
            court.setIsActive(courtDTO.getIsActive());
        }

        CourtEntity updated = courtRepository.save(court);
        return CourtMapper.toCourtDTO(updated);
    }

    @Override
    public void deleteCourtById(Long id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        courtRepository.delete(court);
    }
}
