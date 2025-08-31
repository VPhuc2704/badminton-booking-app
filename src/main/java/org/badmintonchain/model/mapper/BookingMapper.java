package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.entity.CustomerEntity;

import java.util.UUID;

public class BookingMapper {

    // Entity -> DTO
    public static BookingDTO toBookingDTO(BookingsEntity bookingEntity) {
        BookingDTO booking = new BookingDTO();
        booking.setId(bookingEntity.getId());
        booking.setBookingCode(UUID.randomUUID().toString().substring(0, 10));
        booking.setCustomer(
                CustomerMapper.toCustomerDTO(bookingEntity.getCustomer())
        );
        booking.setCourt(
                CourtMapper.toCourtDTO(bookingEntity.getCourt())
        );
        booking.setBookingDate(bookingEntity.getBookingDate());
        booking.setStartTime(bookingEntity.getStartTime());
        booking.setEndTime(bookingEntity.getEndTime());
        booking.setTotalAmount(bookingEntity.getTotalAmount());
        booking.setStatus(bookingEntity.getStatus());
        return booking;
    }

    // DTO -> Entity
    public static BookingsEntity toBookingsEntity(BookingDTO bookingDTO, CustomerEntity customer, CourtEntity court) {
        BookingsEntity booking = new BookingsEntity();
        booking.setId(bookingDTO.getId());
        booking.setBookingCode(
                bookingDTO.getBookingCode() != null
                        ? bookingDTO.getBookingCode()
                        : UUID.randomUUID().toString().substring(0, 10)
        );
        booking.setCustomer(customer);
        booking.setCourt(court);
        booking.setBookingDate(bookingDTO.getBookingDate());
        booking.setStartTime(bookingDTO.getStartTime());
        booking.setEndTime(bookingDTO.getEndTime());
        booking.setTotalAmount(bookingDTO.getTotalAmount());
        booking.setStatus(bookingDTO.getStatus());

        return booking;
    }
}
