package org.badmintonchain.service;

import org.badmintonchain.model.dto.requests.LoginRequestDTO;
import org.badmintonchain.model.dto.requests.RegisterRequestDTO;
import org.badmintonchain.model.dto.response.UserInfoDTO;
import org.badmintonchain.model.entity.UsersEntity;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginRequestDTO request);
    UserInfoDTO createUser(RegisterRequestDTO request);
    void logout(String email, String refreshToken);
    Map<String, Object> refreshToken(String refreshTokenValue);
}
