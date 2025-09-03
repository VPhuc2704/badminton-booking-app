package org.badmintonchain.repository;

import org.badmintonchain.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {
    Optional<CustomerEntity> findByUsersId(Long usersId);
    Optional<CustomerEntity> findByUsers_Id(Long usersId);
}
