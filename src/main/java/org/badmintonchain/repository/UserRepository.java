package org.badmintonchain.repository;

import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<UsersEntity,Long> {
//    Optional<UsersEntity> findUserByEmail(String email);
    Optional<UsersEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<UsersEntity> findAllByRoleName(RoleName roleName, Pageable pageable);

    @Query("""
           SELECT u FROM UsersEntity u
           WHERE u.roleName = :role
             AND (:isActive IS NULL OR u.isActive = :isActive)
             AND (
                 :keyword IS NULL 
                 OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
             )
           """)
    Page<UsersEntity> findAllCustomers(@Param("role") RoleName role,
                                       @Param("keyword") String keyword,
                                       @Param("isActive") Boolean isActive,
                                       Pageable pageable);
}
