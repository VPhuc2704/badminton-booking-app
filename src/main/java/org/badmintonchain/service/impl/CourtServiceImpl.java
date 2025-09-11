package org.badmintonchain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.model.mapper.CourtMapper;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourtServiceImpl implements CourtService {
    @Autowired
    private CourtRepository courtRepository;

    // Admin: lấy tất cả sân
    @Override
    public PageResponse<CourtDTO> getAllCourts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<CourtEntity> courts = courtRepository.findAll(pageable);

        Page<CourtDTO> dtoPage = courts.map(CourtMapper::toCourtDTO);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
    }

    // User: chỉ lấy sân active
    @Override
    public PageResponse<CourtDTO> getAllActiveCourts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<CourtEntity> courts = courtRepository.findByIsActiveTrueAndStatus(CourtStatus.AVAILABLE, pageable);

        Page<CourtDTO> dtoPage = courts.map(CourtMapper::toCourtDTO);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
    }

    @Override
    public CourtDTO getCourtById(Long id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(()-> new CourtException("Court not found with id " + id));
        CourtDTO courtDTO = CourtMapper.toCourtDTO(court);
        return courtDTO;
    }

    @Override
    public CourtDTO getCourtIfAvailable(Long id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new CourtException("Court not found"));

        if (!court.getIsActive() || court.getStatus() != CourtStatus.AVAILABLE) {
            throw new CourtException("Court is not available");
        }
        return CourtMapper.toCourtDTO(court);
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
                .orElseThrow(() -> new CourtException("Court not found"));

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
        if (courtDTO.getStatus() != null) {
            court.setStatus(courtDTO.getStatus());
        }

        if (!court.getIsActive() && court.getStatus() == CourtStatus.AVAILABLE) {
            throw new CourtException("Inactive court cannot be AVAILABLE");
        }

        CourtEntity updated = courtRepository.save(court);
        return CourtMapper.toCourtDTO(updated);
    }

    @Override
    public void deleteCourtById(Long id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new CourtException("Court not found"));
        courtRepository.delete(court);
    }
}
