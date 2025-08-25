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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
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
        // 1. Xác thực người dùng
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Lấy thông tin sân
        CourtEntity court = courtRepository.findById(bookingRequest.getCourt().getId())
                .orElseThrow(() -> new CourtException("Court not found"));

        // 3. Kiểm tra hoặc tạo mới khách hàng (customer) nếu chưa có
        CustomerEntity customer = getOrCreateCustomer(user, bookingRequest);

        // 4. Kiểm tra xung đột lịch đặt sân
        Boolean conflictExists  = bookingRepository.existsConflictingBookings(
                court.getId(),
                bookingRequest.getBookingDate(),
                bookingRequest.getStartTime(),
                bookingRequest.getEndTime()
        );
        if (conflictExists) {
            throw new CourtException("Court already booked during this time slot!");
        }

        // 5. Tính tổng tiền (dựa trên giờ đặt và giá theo giờ của sân)
        BigDecimal totalPrice = calculateBookingPrice(court, bookingRequest.getStartTime(), bookingRequest.getEndTime());

        // 6. Chuyển từ DTO → Entity và set thêm thông tin
        BookingsEntity booking = BookingMapper.toBookingsEntity(bookingRequest, customer, court);
        booking.setTotalAmount(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        // 7. Lưu booking vào DB
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

    private CustomerEntity getOrCreateCustomer(UsersEntity user, BookingDTO bookingRequest) {
        return customerRepository.findByUsersId(user.getId())
                .orElseGet(() -> {
                    CustomerEntity customerEntity = new CustomerEntity();
                    customerEntity.setUsers(user);
                    customerEntity.setNumberPhone(bookingRequest.getCustomer().getNumberPhone());
                    return customerRepository.save(customerEntity);
                });
    }

    private BigDecimal calculateBookingPrice(CourtEntity court, LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        double hours = minutes / 60.0;
        return court.getHourlyRate().multiply(BigDecimal.valueOf(hours));

    }
}
