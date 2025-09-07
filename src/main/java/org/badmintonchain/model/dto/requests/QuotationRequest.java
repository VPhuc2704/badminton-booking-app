package org.badmintonchain.model.dto.requests;

import lombok.Data;

@Data
public class QuotationRequest {
    private Long serviceId;
    private int quantity;
    private String customerEmail;
    private String customerPhone;
}
