package org.badmintonchain.repository;

import org.badmintonchain.model.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<UsersEntity,Long> {
//    Optional<UsersEntity> findUserByEmail(String email);
    Optional<UsersEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
