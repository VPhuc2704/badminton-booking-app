package org.badmintonchain.repository;

import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.entity.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<CourtEntity, Long> {
    List<CourtEntity> findByIsActiveTrue();
}
