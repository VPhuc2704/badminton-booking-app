package org.badmintonchain.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.badmintonchain.model.dto.requests.LoginRequestDTO;
import org.badmintonchain.model.dto.requests.RegisterRequestDTO;
import org.badmintonchain.model.dto.response.LoginResponse;
import org.badmintonchain.model.entity.RefreshToken;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.entity.VerificationToken;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.repository.VerificationTokenRepository;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.security.JwtTokenProvider;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        UsersEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account not verified. Please check your email.");
        }

        // Save refresh token to database
        refreshTokenService.createRefreshToken(user, refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("type", "Bearer");
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRoleName().name());

        return response;
    }

    @Override
    public UsersEntity createUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        UsersEntity user = new UsersEntity();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
//        user.setNumberPhone(request.getPhone());
        user.setRoleName(RoleName.CUSTOMMER);
        user.setActive(false);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
        return user;
    }

    @Override
    @Transactional
    public void logout(String email, String refreshToken) {
        try {

            // Find user by email
            UsersEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Revoke refresh token in database
            if (refreshToken != null && !refreshToken.isEmpty()) {
                refreshTokenService.revokeRefreshToken(refreshToken);
                log.info("Refresh token revoked for user: {}", email);
            }

            // Xóa tất cả refresh token
            refreshTokenService.deleteByUser(user);

            // Clear security context
            SecurityContextHolder.clearContext();

            log.info("User {} logged out successfully", email);

        } catch (Exception e) {
            log.error("Error during logout for user {}: {}", email, e.getMessage());
            // Still clear context even if there's an error
            SecurityContextHolder.clearContext();
        }
    }
    @Override
    public Map<String, Object> refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshTokenService.verifyExpiration(refreshToken);

        UsersEntity user = refreshToken.getUser();

        UserDetails userDetails = new CustomUserDetails(user);

        // Tạo access token mới
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", refreshToken.getRefreshToken()); // giữ lại refresh token
        response.put("type", "Bearer");
        return response;
    }

}
