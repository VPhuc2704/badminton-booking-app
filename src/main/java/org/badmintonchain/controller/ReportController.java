package org.badmintonchain.controller;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.ReportResponse;
import org.badmintonchain.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/date")
    public ReportResponse getDashboardByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.getDashboardByDate(date);
    }

    @GetMapping("/month")
    public ReportResponse getDashboardByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        return reportService.getDashboardByMonth(month, year);
    }
}
