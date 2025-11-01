package org.badmintonchain.repository;

import org.badmintonchain.model.entity.MaintenanceReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceReportRepository extends JpaRepository<MaintenanceReportEntity, Long> {
}
