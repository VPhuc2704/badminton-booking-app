package org.badmintonchain.repository;

import org.badmintonchain.model.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByBooking_Id(Long bookingId);
}
