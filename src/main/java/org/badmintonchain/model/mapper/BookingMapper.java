package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.entity.CustomerEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.utils.GenerateBookingCode;

import java.time.Duration;
import java.util.UUID;

public class BookingMapper {

    // Entity -> DTO
    public static BookingDTO toBookingDTO(BookingsEntity bookingEntity) {
        BookingDTO booking = new BookingDTO();
        booking.setId(bookingEntity.getId());
        booking.setBookingCode(bookingEntity.getBookingCode());
        booking.setCustomer(
                CustomerMapper.toCustomerDTO(bookingEntity.getCustomer())
        );
        booking.setCourt(
                CourtMapper.toCourtDTO(bookingEntity.getCourt())
        );
        booking.setBookingDate(bookingEntity.getBookingDate());
        booking.setStartTime(bookingEntity.getStartTime());
        booking.setEndTime(bookingEntity.getEndTime());
        long minutes = Duration.between(bookingEntity.getStartTime(), bookingEntity.getEndTime()).toMinutes();
        booking.setDuration(minutes/60.0);
        booking.setTotalAmount(bookingEntity.getTotalAmount());
        booking.setStatus(bookingEntity.getStatus());
        booking.setPaymentStatus(bookingEntity.getPaymentStatus());
        booking.setPaymentMethod(
                bookingEntity.getTransaction() != null
                ? bookingEntity.getTransaction().getPaymentMethod()
                : null
        );
        booking.setCreatedAt(bookingEntity.getCreateAt());
        booking.setUpdatedAt(bookingEntity.getUpdateAt());
        return booking;
    }

    // DTO -> Entity
    public static BookingsEntity toBookingsEntity(BookingDTO bookingDTO, CustomerEntity customer, CourtEntity court) {
        BookingsEntity booking = new BookingsEntity();
        booking.setId(bookingDTO.getId());
        booking.setBookingCode(GenerateBookingCode.generate());
        booking.setCustomer(customer);
        booking.setCourt(court);
        booking.setBookingDate(bookingDTO.getBookingDate());
        booking.setStartTime(bookingDTO.getStartTime());
        booking.setEndTime(bookingDTO.getEndTime());
        booking.setTotalAmount(bookingDTO.getTotalAmount());
        booking.setStatus(
                bookingDTO.getStatus() != null ? bookingDTO.getStatus() : BookingStatus.PENDING
        );
        booking.setPaymentStatus(bookingDTO.getPaymentStatus());

        return booking;
    }
}
