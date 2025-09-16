package org.badmintonchain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.model.dto.AvailabilitySlotDTO;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.model.mapper.CourtMapper;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class CourtServiceImpl implements CourtService {
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private BookingRepository bookingRepository;

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
        if (courtDTO.getImages() != null && !courtDTO.getImages().isEmpty()) {
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

    @Override
    public boolean isCourtAvailable(Long courtId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        boolean conflict = bookingRepository.existsConflictingBookings(courtId, date, startTime, endTime);
        return !conflict;
    }

    public List<AvailabilitySlotDTO> getAvailableSlots(Long courtId, LocalDate date) {
        // Lấy danh sách booking của sân theo ngày
        List<BookingsEntity> bookings = bookingRepository
                .findByCourtIdAndBookingDateAndStatusIn(courtId, date,  Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PENDING));

        // Nếu chưa có booking, sân trống cả ngày (giả sử 06:00-22:00)
        List<AvailabilitySlotDTO> slots = new ArrayList<>();
        LocalTime dayStart = LocalTime.of(6, 0);
        LocalTime dayEnd = LocalTime.of(22, 0);

        // Sắp xếp theo giờ bắt đầu
        bookings.sort(Comparator.comparing(BookingsEntity::getStartTime));

        LocalTime current = dayStart;
        for (BookingsEntity b : bookings) {
            if (b.getStartTime().isAfter(current)) {
                slots.add(new AvailabilitySlotDTO(
                        current.format(DateTimeFormatter.ofPattern("HH:mm")),
                        b.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                ));
            }
            // Cập nhật thời điểm hiện tại
            if (b.getEndTime().isAfter(current)) {
                current = b.getEndTime();
            }
        }

        // Thêm khoảng cuối ngày nếu còn trống
        if (current.isBefore(dayEnd)) {
            slots.add(new AvailabilitySlotDTO(
                    current.format(DateTimeFormatter.ofPattern("HH:mm")),
                    dayEnd.format(DateTimeFormatter.ofPattern("HH:mm"))
            ));
        }

        return slots;
    }

}
