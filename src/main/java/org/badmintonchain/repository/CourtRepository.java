package org.badmintonchain.repository;

import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.enums.CourtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<CourtEntity, Long> {
    Page<CourtEntity> findByIsActiveTrueAndStatus(CourtStatus status, Pageable pageable);
    Page<CourtEntity> findByBranchId(Long branchId, Pageable pageable);

    @Query(
            value = "SELECT c.* " +
                    "FROM courts c " +
                    "WHERE NOT EXISTS (" +
                    "   SELECT 1 " +
                    "   FROM bookings b " +
                    "   WHERE b.court_id = c.id " +
                    "     AND b.start_time < :endTime " +
                    "     AND b.end_time > :startTime" +
                    ")",
            nativeQuery = true
    )
    List<CourtEntity> findFreeCourts(
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
