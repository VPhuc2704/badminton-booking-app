package org.badmintonchain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.model.dto.AvailabilitySlotDTO;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.BranchEntity;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.model.mapper.CourtMapper;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.BranchRepository;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.CourtService;
import org.badmintonchain.service.CustomerService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CourtServiceImpl implements CourtService {
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private AuthService  authService;
    @Autowired
    private BranchRepository branchRepository;
    // Admin: lấy tất cả sân
    @Override
    public PageResponse<CourtDTO> getAllCourts(int page, int size) {
        UsersEntity currentUser = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
//        Page<CourtEntity> courts = courtRepository.findAll(pageable);
        Page<CourtEntity> courts;
        if (currentUser.getRoleName() == RoleName.ADMIN) {
            courts = courtRepository.findAll(pageable);
        } else if (currentUser.getRoleName() == RoleName.STAFF) {
            courts = courtRepository.findByBranchId(currentUser.getBranch().getId(), pageable);
        } else {
            throw new CourtException("Bạn không có quyền truy cập");
        }

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
        UsersEntity currentUser = authService.getCurrentUser();
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(()-> new CourtException("Court not found with id " + id));

        if(currentUser.getRoleName() == RoleName.STAFF) {
            Long staffBranchId = currentUser.getBranch().getId();
            Long courtBranchId = court.getBranch().getId();

            if (!staffBranchId.equals(courtBranchId)) {
                throw new CourtException("Bạn không có quyền truy cập sân của chi nhánh khác");
            }
        }

        CourtDTO courtDTO = CourtMapper.toCourtDTO(court);
        return courtDTO;
    }

    // user
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

        UsersEntity currentUser = authService.getCurrentUser();
        CourtEntity courtEntity = CourtMapper.toCourtEntity(court);

        if (currentUser.getRoleName() == RoleName.ADMIN) {
            if(court.getBranchId() == null) {
                throw new CourtException("Vui lòng chọn chi nhánh khi tạo sân");
            }

            BranchEntity branch = branchRepository.findById(court.getBranchId())
                    .orElseThrow(() -> new CourtException("Không tìm thấy chi nhánh"));
            courtEntity.setBranch(branch);

        } else if (currentUser.getRoleName() == RoleName.STAFF) {

            BranchEntity branch = currentUser.getBranch();

            // Staff: chỉ được tạo sân trong chi nhánh của mình
            if (currentUser.getBranch() == null) {
                throw new CourtException("Tài khoản staff chưa được gán chi nhánh.");
            }

            if (Boolean.FALSE.equals(branch.getIsActive())) {
                throw new CourtException("Chi nhánh của bạn đã ngừng hoạt động, không thể tạo sân.");
            }


            courtEntity.setBranch(currentUser.getBranch());
        } else {
            throw new CourtException("Bạn không có quyền tạo sân.");
        }

        CourtEntity saved = courtRepository.save(courtEntity);
        return CourtMapper.toCourtDTO(saved);
    }

    @Override
    public CourtDTO updateCourt(Long id, CourtDTO courtDTO) {

        UsersEntity currentUser = authService.getCurrentUser();

        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new CourtException("Không tìm thấy sân với ID: \" + id"));

        if (currentUser.getRoleName() == RoleName.STAFF) {
            BranchEntity staffBranch = currentUser.getBranch();

            if (staffBranch == null) {
                throw new CourtException("Tài khoản staff chưa được gán chi nhánh.");
            }

            // Chỉ được phép cập nhật sân của chi nhánh mình quản lý
            if (!staffBranch.getId().equals(court.getBranch().getId())) {
                throw new CourtException("Bạn không có quyền chỉnh sửa sân của chi nhánh khác.");
            }

            // Chỉ cập nhật nếu chi nhánh của staff đang hoạt động
            if (Boolean.FALSE.equals(staffBranch.getIsActive())) {
                throw new CourtException("Chi nhánh của bạn đã ngừng hoạt động, không thể cập nhật sân.");
            }
        }

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
            throw new CourtException("Sân không hoạt động không thể đặt trạng thái là AVAILABLE.");
        }

        CourtEntity updated = courtRepository.save(court);
        return CourtMapper.toCourtDTO(updated);
    }

    @Override
    public void deleteCourtById(Long id) {
        UsersEntity currentUser = authService.getCurrentUser();
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new CourtException("Không tìm thấy sân với ID: " + id));

        if (currentUser.getRoleName() == RoleName.STAFF) {
            BranchEntity staffBranch = currentUser.getBranch();

            if (staffBranch == null) {
                throw new CourtException("Tài khoản staff chưa được gán chi nhánh.");
            }

            // Không cho xoá sân chi nhánh khác
            if (!staffBranch.getId().equals(court.getBranch().getId())) {
                throw new CourtException("Bạn không có quyền xoá sân của chi nhánh khác.");
            }

            // Không cho xoá nếu chi nhánh ngừng hoạt động
            if (Boolean.FALSE.equals(staffBranch.getIsActive())) {
                throw new CourtException("Chi nhánh của bạn đã ngừng hoạt động, không thể xoá sân.");
            }
        }

        // Admin thì không giới hạn
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

    @Override
    public Long mapCourtNameToId(String userInput) {
        String lower = userInput.toLowerCase();

        // Regex: tìm "sân cầu lông số X"
        Pattern p = Pattern.compile("sân(\\s*cầu lông)?\\s*số\\s*(\\d+)");
        Matcher m = p.matcher(lower);
        if (m.find()) {
            int number = Integer.parseInt(m.group(2));
            // Giả sử id = số sân luôn
            if (courtRepository.existsById((long) number)) {
                return (long) number;
            }
        }

        // Nếu không match regex → fallback tìm gần giống trong DB
        List<CourtEntity> courts = courtRepository.findAll();
        for (CourtEntity court : courts) {
            if (lower.contains(court.getCourtName().toLowerCase())) {
                return court.getId();
            }
        }

        return null; // Không tìm thấy
    }

    @Override
    public List<CourtDTO> getFreeCourts(LocalTime start, LocalTime end) {
        List<CourtEntity> courts = courtRepository.findFreeCourts(start, end);

        return courts.stream()
                .map(CourtMapper::toCourtDTO)
                .collect(Collectors.toList());
    }

}
