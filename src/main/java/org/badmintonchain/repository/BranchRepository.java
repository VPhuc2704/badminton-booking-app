package org.badmintonchain.repository;

import org.badmintonchain.model.entity.BranchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<BranchEntity, Long> {
    boolean existsByManager_Id(Long managerId);
    Page<BranchEntity> findAll(Pageable pageable);
    BranchEntity findByManager_Id(Long userId);
}
