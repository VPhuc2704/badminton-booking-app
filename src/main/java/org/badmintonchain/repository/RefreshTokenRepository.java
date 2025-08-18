package org.badmintonchain.repository;

import org.badmintonchain.model.entity.RefreshToken;
import org.badmintonchain.model.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByRefreshToken(String token);
    void deleteByUser(UsersEntity user);
    boolean existsByRefreshTokenAndRevoked(String refreshToken, boolean revoked);
}
