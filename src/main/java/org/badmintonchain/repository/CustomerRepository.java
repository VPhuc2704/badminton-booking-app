package org.badmintonchain.repository;

import org.badmintonchain.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}
