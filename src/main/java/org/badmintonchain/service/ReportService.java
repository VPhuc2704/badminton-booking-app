package org.badmintonchain.service;

import org.badmintonchain.model.dto.ReportResponse;

import java.time.LocalDate;

public interface ReportService {
    ReportResponse getDashboardByDate(LocalDate date);
    ReportResponse getDashboardByMonth(int month, int year);
}
