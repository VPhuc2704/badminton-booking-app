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
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingsEntity, Long>{

    @Query("""
        SELECT b FROM BookingsEntity b
        WHERE (:year IS NULL OR EXTRACT(YEAR FROM b.bookingDate) = :year)
          AND (:month IS NULL OR EXTRACT(MONTH FROM b.bookingDate) = :month)
          AND ( b.bookingDate = COALESCE(:day, b.bookingDate) )
    """)
    Page<BookingsEntity> findByYearMonthDay(@Param("year") Integer year,
                                            @Param("month") Integer month,
                                            @Param("day") LocalDate day,
                                            Pageable pageable);

//    Page<BookingsEntity> findByYearMonthDay(@Param("year") Integer year,
//                                            @Param("month") Integer month,
//                                            @Param("day") LocalDate day,
//                                            Pageable pageable);


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


    List<BookingsEntity> findAllByBookingDate(LocalDate date);
    List<BookingsEntity> findAllByBookingDateAndCourt_Id(LocalDate date, Long courtId);
    List<BookingsEntity> findAllByBookingDateAndCourt_IdAndStatus(LocalDate date, Long courtId, BookingStatus status);
}
