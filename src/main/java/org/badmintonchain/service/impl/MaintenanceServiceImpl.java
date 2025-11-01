package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.requests.MaintenanceReportRequest;
import org.badmintonchain.model.dto.response.MaintenanceReportResponse;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.entity.MaintenanceReportEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.repository.MaintenanceReportRepository;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.MaintenanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private final CourtRepository courtRepository;
    private final MaintenanceReportRepository maintenanceReportRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public String reportDamage(MaintenanceReportRequest request) {
        UsersEntity currentUser = authService.getCurrentUser();

        CourtEntity court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sân với ID: " + request.getCourtId()));

        if (court.getStatus() != CourtStatus.MAINTENANCE) {
            throw new RuntimeException("Chỉ có thể báo cáo khi sân đang ở trạng thái bảo trì.");
        }

        MaintenanceReportEntity report = MaintenanceReportEntity.builder()
                .court(court)
                .reporterName(currentUser.getFullName())
                .description(request.getDescription())
                .build();

        maintenanceReportRepository.save(report);
        return "Báo cáo bảo trì đã được gửi cho admin.";
    }

    @Override
    public List<MaintenanceReportResponse> getReport() {
        List<MaintenanceReportEntity> reports = maintenanceReportRepository.findAll();

        return reports.stream().map(report -> MaintenanceReportResponse.builder()
                .id(report.getId())
                .courtName(report.getCourt().getCourtName())
                .branchName(report.getCourt().getBranch().getBranchName())
                .reporterName(report.getReporterName())
                .description(report.getDescription())
                .build()
        ).toList();
    }
}
