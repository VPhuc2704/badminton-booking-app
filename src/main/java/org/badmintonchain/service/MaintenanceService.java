package org.badmintonchain.service;

import org.badmintonchain.model.dto.requests.MaintenanceReportRequest;
import org.badmintonchain.model.dto.response.MaintenanceReportResponse;

import java.util.List;

public interface MaintenanceService {
    String reportDamage(MaintenanceReportRequest request);
    List<MaintenanceReportResponse> getReport();
}
