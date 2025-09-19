package org.badmintonchain.repository;

import org.badmintonchain.model.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByBooking_Id(Long bookingId);

    // Doanh thu theo ngày
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.booking.paymentStatus = 'PAID' AND t.transactionDate = :date")
    BigDecimal getRevenueByDate(LocalDate date);

    // Doanh thu theo tháng
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.booking.paymentStatus = 'PAID' " +
            "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal getRevenueByMonth(int month, int year);

    // Doanh thu theo giờ (ngày)
    @Query("SELECT HOUR(t.booking.startTime), SUM(t.amount) " +
            "FROM TransactionEntity t " +
            "WHERE t.booking.paymentStatus = 'PAID' AND t.transactionDate = :date " +
            "GROUP BY HOUR(t.booking.startTime) ORDER BY HOUR(t.booking.startTime)")
    List<Object[]> getRevenueByHour(LocalDate date);

    // Doanh thu theo ngày (tháng)
    @Query("SELECT DAY(t.transactionDate), SUM(t.amount) " +
            "FROM TransactionEntity t " +
            "WHERE t.booking.paymentStatus = 'PAID' " +
            "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year " +
            "GROUP BY DAY(t.transactionDate) ORDER BY DAY(t.transactionDate)")
    List<Object[]> getRevenueByDayInMonth(int month, int year);

}
