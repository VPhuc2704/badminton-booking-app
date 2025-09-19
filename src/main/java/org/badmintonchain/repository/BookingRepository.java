package org.badmintonchain.repository;

import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingsEntity, Long>{

    @Query(value = """
        SELECT * FROM bookings b
        WHERE (:year IS NULL OR EXTRACT(ISOYEAR FROM b.booking_date) = :year)
          AND (:month IS NULL OR EXTRACT(MONTH FROM b.booking_date) = :month)
          AND (:week IS NULL OR EXTRACT(WEEK FROM b.booking_date) = :week)
          AND (b.booking_date = COALESCE(:day, b.booking_date))
        """,
            nativeQuery = true
    )
    Page<BookingsEntity> findByYearMonthDay(@Param("year") Integer year,
                                            @Param("month") Integer month,
                                            @Param("week") Integer week,
                                            @Param("day") LocalDate day,
                                            Pageable pageable);


    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
            "FROM BookingsEntity b " +
            "WHERE b.court.id = :courtId " +
            "AND b.bookingDate = :bookingDate " +
            "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsConflictingBookings(
            @Param("courtId") Long courtId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    List<BookingsEntity> findAllByCustomer_Users_Id(Long userId);
    BookingsEntity findByIdAndCustomer_Users_Id(Long id, Long userId);
    Boolean existsByCustomer_Id(Long id);

    List<BookingsEntity> findByCourtIdAndBookingDateAndStatusIn(Long courtId, LocalDate date, List<BookingStatus> statuses);
    List<BookingsEntity> findByCourt_CourtNameAndBookingDateAndStatusIn(String courtName, LocalDate date, List<BookingStatus> statuses);
    List<BookingsEntity> findAllByBookingDate(LocalDate date);
    List<BookingsEntity> findAllByBookingDateAndCourt_Id(LocalDate date, Long courtId);
    List<BookingsEntity> findAllByBookingDateAndCourt_IdAndStatus(LocalDate date, Long courtId, BookingStatus status);

    // Đếm số booking trong 1 ngày
    long countByBookingDate(LocalDate date);

    // Đếm số booking trong 1 ngày theo status
    long countByBookingDateAndStatus(LocalDate date, BookingStatus status);

    // Đếm số booking trong 1 tháng
    @Query("SELECT COUNT(b) FROM BookingsEntity b " +
            "WHERE MONTH(b.bookingDate) = :month AND YEAR(b.bookingDate) = :year")
    long countByMonthAndYear(int month, int year);

    // Đếm số booking trong 1 tháng theo status
    @Query("SELECT COUNT(b) FROM BookingsEntity b " +
            "WHERE MONTH(b.bookingDate) = :month AND YEAR(b.bookingDate) = :year " +
            "AND b.status = :status")
    long countByMonthAndYearAndStatus(int month, int year, BookingStatus status);
}
