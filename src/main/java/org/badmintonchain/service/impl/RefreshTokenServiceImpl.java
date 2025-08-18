package org.badmintonchain.service.impl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.entity.RefreshToken;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.repository.RefreshTokenRepository;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UsersEntity user, String refreshTokenValue) {
        // Delete existing refresh token for user
        deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was revoked. Please make a new signin request");
        }
        return token;
    }

    @Override
    public void deleteByUser(UsersEntity user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken rt = refreshToken.get();
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        return refreshToken.map(RefreshToken::isRevoked).orElse(true);
    }
}
