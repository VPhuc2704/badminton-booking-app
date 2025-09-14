package org.badmintonchain.model.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long customerId;
    private String numberPhone;
    private String email;
    private String fullname;
}
