package org.badmintonchain.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.badmintonchain.model.entity.CourtEntity;

@Data
@Builder
public class MaintenanceReportResponse {
    private Long id;
    private String courtName;
    private String reporterName;
    private String branchName;
    private String description;
}
