package org.badmintonchain.controller;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.ReportResponse;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class ReportController {
    private final ReportService reportService;
    private final AuthService authService;

    @GetMapping("/date")
    public ReportResponse getDashboardByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.getDashboardByDate(date);
    }

    @GetMapping("/month")
    public ReportResponse getDashboardByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        return reportService.getDashboardByMonth(month, year);
    }

    @GetMapping("/range")
    public ReportResponse getDashboardByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long branchId
    ) {
        // Nếu endDate không truyền → mặc định bằng startDate
        if (endDate == null) {
            endDate = startDate;
        }

        UsersEntity currentUser = authService.getCurrentUser();

        // Nếu user là STAFF → luôn lấy branch của họ, ignore branchId truyền từ client
        if (currentUser.getRoleName() == RoleName.STAFF && currentUser.getBranch() != null) {
            branchId = currentUser.getBranch().getId();
        }

        return reportService.getDashboardByDateRange(startDate, endDate,  branchId);
    }
}
