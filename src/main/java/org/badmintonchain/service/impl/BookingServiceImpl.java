package org.badmintonchain.service.impl;

import jakarta.transaction.Transactional;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.entity.CustomerEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.mapper.BookingMapper;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.CourtRepository;
import org.badmintonchain.repository.CustomerRepository;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private BookingRepository bookingRepository;


    @Override
    public BookingDTO createBoking(BookingDTO bookingRequest,  Long userId) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CourtEntity court = courtRepository.findById(bookingRequest.getCourt().getId())
                .orElseThrow(() -> new CourtException("Court not found"));

        CustomerEntity customer = customerRepository.findByUsersId(user.getId())
                .orElseGet(() -> {
                    CustomerEntity customerEntity = new CustomerEntity();
                    customerEntity.setUsers(user);
                    customerEntity.setNumberPhone(bookingRequest.getCustomer().getNumberPhone());
                    return customerRepository.save(customerEntity);
                });

        List<BookingsEntity> conflicts = bookingRepository.findConflictingBookings(
                court.getId(),
                bookingRequest.getBookingDate(),
                bookingRequest.getStartTime(),
                bookingRequest.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new CourtException("Court already booked during this time slot!");
        }

        BookingsEntity booking = BookingMapper.toBookingsEntity(bookingRequest, customer, court);

        booking.setStatus(BookingStatus.PENDING);

        BookingsEntity saved = bookingRepository.save(booking);


//        EmailEntity emailLog = new EmailLogEntity();
//        emailLog.setBookingId(savedBooking.getBookingId());
//        emailLog.setRecipientEmail(user.getEmail());
//        emailLog.setSubject("Xác nhận đặt sân #" + savedBooking.getBookingCode());
//        emailLog.setEmailType("BOOKING_CONFIRMATION");
//        emailLog.setTemplateUsed("booking_confirmation");
//        emailLogRepo.save(emailLog);

//        return savedBooking;

        return BookingMapper.toBookingDTO(saved);
    }
}
