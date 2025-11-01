package org.badmintonchain.controller;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.requests.MaintenanceReportRequest;
import org.badmintonchain.model.dto.response.MaintenanceReportResponse;
import org.badmintonchain.service.MaintenanceService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PreAuthorize("hasAnyRole('STAFF')")
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> getReportDamage(@RequestBody MaintenanceReportRequest request) {
        String result = maintenanceService.reportDamage(request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Bao cao chi tiet thiet hai thanh cong",
                HttpStatus.CREATED.value(),
                result,
                "/api/maintenance/report"
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<MaintenanceReportResponse>>> reportDamage() {
        List<MaintenanceReportResponse> result = maintenanceService.getReport();
        return ResponseEntity.ok(new ApiResponse<>(
                "Bao cao chi tiet thiet hai thanh cong",
                HttpStatus.OK.value(),
                result,
                "/api/maintenance/report"
        ));
    }
}
