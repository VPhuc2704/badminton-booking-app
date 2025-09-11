package org.badmintonchain.repository;

import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.enums.CourtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<CourtEntity, Long> {
    Page<CourtEntity> findByIsActiveTrueAndStatus(CourtStatus status, Pageable pageable);
}
