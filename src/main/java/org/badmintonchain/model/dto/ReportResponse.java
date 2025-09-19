package org.badmintonchain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private long totalBookings;
    private long completedBookings;
    private long newCustomers;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueChart; // key: giờ/ngày, value: doanh thu
}
