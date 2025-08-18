package org.badmintonchain.service;

import org.badmintonchain.model.entity.RefreshToken;
import org.badmintonchain.model.entity.UsersEntity;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UsersEntity user, String refreshTokenValue);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(UsersEntity user);
    void revokeRefreshToken(String token);
    boolean isTokenRevoked(String token);
}
