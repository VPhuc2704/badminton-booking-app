package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.ReportResponse;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.CustomerRepository;
import org.badmintonchain.repository.TransactionRepository;
import org.badmintonchain.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingsRepository;
    private final CustomerRepository customerRepository;

    @Override
    public ReportResponse getDashboardByDate(LocalDate date) {
        long totalBookings = bookingsRepository.countByBookingDate(date);
        long completedBookings = bookingsRepository.countByBookingDateAndStatus(date, BookingStatus.CONFIRMED);
        long newCustomers = customerRepository.countNewCustomersByDate(date);
        BigDecimal totalRevenue = transactionRepository.getRevenueByDate(date);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Doanh thu theo giờ 6 → 22
        List<Object[]> results = transactionRepository.getRevenueByHour(date);
        Map<String, BigDecimal> chart = new LinkedHashMap<>();
        for (int h = 6; h <= 22; h++) {
            chart.put(h + "h", BigDecimal.ZERO);
        }
        for (Object[] row : results) {
            int hour = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (hour >= 6 && hour <= 22) {
                chart.put(hour + "h", amount);
            }
        }

        return new ReportResponse(totalBookings, completedBookings, newCustomers, totalRevenue, chart);
    }

    @Override
    public ReportResponse getDashboardByMonth(int month, int year) {
        long totalBookings = bookingsRepository.countByMonthAndYear(month, year);
        long completedBookings = bookingsRepository.countByMonthAndYearAndStatus(month, year, BookingStatus.CONFIRMED);
        long newCustomers = customerRepository.countNewCustomersByMonth(month, year);
        BigDecimal totalRevenue = transactionRepository.getRevenueByMonth(month, year);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Doanh thu theo ngày trong tháng
        List<Object[]> results = transactionRepository.getRevenueByDayInMonth(month, year);
        Map<String, BigDecimal> chart = new LinkedHashMap<>();
        for (Object[] row : results) {
            int day = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            chart.put("Day " + day, amount);
        }

        return new ReportResponse(totalBookings, completedBookings, newCustomers, totalRevenue, chart);
    }
}
