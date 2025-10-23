package org.badmintonchain.repository;

import org.badmintonchain.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {
    Optional<CustomerEntity> findByUsersId(Long usersId);
    Optional<CustomerEntity> findByUsers_Id(Long usersId);

    @Query("SELECT COUNT(c) FROM CustomerEntity c " +
            "WHERE DATE(c.createAt) = :date")
    long countNewCustomersByDate(LocalDate date);

    @Query("SELECT COUNT(c) FROM CustomerEntity c " +
            "WHERE MONTH(c.createAt) = :month AND YEAR(c.createAt) = :year")
    long countNewCustomersByMonth(int month, int year);

    @Query("""
    SELECT COUNT(DISTINCT c)
    FROM CustomerEntity c
    JOIN c.bookings b
    WHERE DATE(c.createAt) BETWEEN :startDate AND :endDate
    AND (:branchId IS NULL OR b.court.branch.id = :branchId)
""")
    long countNewCustomersBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId);



}
