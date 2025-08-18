package org.badmintonchain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoDTO {
    private String fullName;
    private String email;
    private String role;
}
