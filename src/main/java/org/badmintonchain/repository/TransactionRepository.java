package org.badmintonchain.repository;

import org.badmintonchain.model.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Tổng doanh thu (nếu branchId null => toàn hệ thống, ngược lại => lọc chi nhánh)
     @Query("""
        SELECT SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.booking.paymentStatus = 'PAID'
          AND t.transactionDate BETWEEN :startDate AND :endDate
          AND (:branchId IS NULL OR t.booking.court.branch.id = :branchId)
    """)
    BigDecimal getRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("branchId") Long branchId);

    // Doanh thu theo ngày
    @Query("""
        SELECT t.transactionDate, SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.booking.paymentStatus = 'PAID'
          AND t.transactionDate BETWEEN :startDate AND :endDate
          AND (:branchId IS NULL OR t.booking.court.branch.id = :branchId)
        GROUP BY t.transactionDate
        ORDER BY t.transactionDate
    """)
    List<Object[]> getRevenueByDayBetweenDates(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("branchId") Long branchId);

    // Doanh thu theo giờ
    @Query("""
        SELECT HOUR(t.booking.startTime), SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.booking.paymentStatus = 'PAID'
          AND t.transactionDate BETWEEN :startDate AND :endDate
          AND (:branchId IS NULL OR t.booking.court.branch.id = :branchId)
        GROUP BY HOUR(t.booking.startTime)
        ORDER BY HOUR(t.booking.startTime)
    """)
    List<Object[]> getRevenueByHours(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("branchId") Long branchId);


}
