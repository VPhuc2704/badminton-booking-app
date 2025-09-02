package org.badmintonchain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class UserInfoDTO {
    private String fullName;
    private String email;
    private String role;
}
