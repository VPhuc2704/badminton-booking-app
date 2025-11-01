package org.badmintonchain.model.dto.requests;

import lombok.Data;

@Data
public class MaintenanceReportRequest {
    private Long courtId;
    private String description; // mô tả thiệt hại
}
