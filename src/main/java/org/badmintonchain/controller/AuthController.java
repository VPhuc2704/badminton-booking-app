package org.badmintonchain.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.badmintonchain.model.dto.requests.LoginRequestDTO;
import org.badmintonchain.model.dto.requests.RegisterRequestDTO;
import org.badmintonchain.model.dto.response.LoginResponse;
import org.badmintonchain.model.dto.response.TokenDTO;
import org.badmintonchain.model.dto.response.UserInfoDTO;
import org.badmintonchain.model.entity.UsersEntity;

import org.badmintonchain.security.JwtTokenProvider;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.RefreshTokenService;
import org.badmintonchain.service.VerificationTokenService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private VerificationTokenService  verificationTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestDTO request) {
        Map<String, Object> auth = authService.login(request);

        UserInfoDTO userInfo = new UserInfoDTO(
                (String) auth.get("fullName"),
                (String) auth.get("email"),
                (String) auth.get("role"),
                (String) auth.get("numberPhone")
        );

        TokenDTO tokens = new TokenDTO(
                "Bearer",
                (String) auth.get("accessToken"),
                (String) auth.get("refreshToken")
        );

        LoginResponse loginResponse = new LoginResponse(userInfo, tokens);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                "Login successful",
                200,
                loginResponse,
                "/api/auth/login"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            UserInfoDTO user = authService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("User registered successfully. Please check your email to verify account.", 201, user, "/api/auth/register"));
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Registration failed", 400, null, "/api/auth/register"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        String result = verificationTokenService.verifyAccount(token);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorization,
                                    @RequestBody String refreshToken) {
        try {
            // Lấy email từ access token
            String accessToken = authorization.replace("Bearer ", "");
            String email = jwtTokenProvider.getUsernameFromToken(accessToken);

            // Logout
            authService.logout(email, refreshToken);

            return ResponseEntity.ok().body("Logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Logout failed");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            Map<String, Object> auth = authService.refreshToken(refreshToken);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Token refreshed successfully",
                    200,
                    auth,
                    "/api/auth/refresh"
            ));
        } catch (Exception e) {
            log.error("Refresh token failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Refresh token invalid or expired", 401, null, "/api/auth/refresh"));
        }
    }

}
