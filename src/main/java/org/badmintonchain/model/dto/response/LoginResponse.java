package org.badmintonchain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private UserInfoDTO  userInfo;
    private TokenDTO  token;
}
