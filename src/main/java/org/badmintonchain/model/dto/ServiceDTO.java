package org.badmintonchain.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceDTO {
    private Long serviceId;
    private String serviceName;
    private String serviceType;
    private String description;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
