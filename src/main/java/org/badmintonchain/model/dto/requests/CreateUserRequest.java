package org.badmintonchain.model.dto.requests;

import lombok.Data;
import org.badmintonchain.model.enums.RoleName;


@Data
public class CreateUserRequest {
    private String fullName;
    private String email;
    private RoleName role;
    private String numberPhone;
}
