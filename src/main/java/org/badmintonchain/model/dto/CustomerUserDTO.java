package org.badmintonchain.model.dto;

import lombok.Data;

@Data
public class CustomerUserDTO {
    private Long userId;
    private Long customerId;
    private String email;
    private String fullName;
    private String numberPhone;
    private Boolean active;
    private String roleName;
}


// check lại nếu customer sau khi đặt đơn thì có số điện thoại đặt lại đơn mới thì thay so dt đc khong nhá