package org.badmintonchain.service.impl;

import jakarta.transaction.Transactional;
import org.badmintonchain.exceptions.BookingException;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.model.dto.BookingDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.entity.*;
import org.badmintonchain.model.enums.BookingStatus;
import org.badmintonchain.model.enums.EmailType;
import org.badmintonchain.model.enums.PaymentMethod;
import org.badmintonchain.model.enums.PaymentStatus;
import org.badmintonchain.model.mapper.BookingMapper;
import org.badmintonchain.repository.*;
import org.badmintonchain.service.BookingService;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.event.BookingCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EmailService emailService;


    @Override
    public BookingDTO createBoking(BookingDTO bookingRequest,  Long userId) {
        // 1. Xác thực người dùng
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Lấy thông tin sân
        CourtEntity court = courtRepository.findById(bookingRequest.getCourt().getId())
                .orElseThrow(() -> new CourtException("Court not found"));

        if (!court.getIsActive()) {
            throw new CourtException("Court " + court.getCourtName() + " is not available for booking.");
        }

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
        booking.setPaymentStatus(PaymentStatus.UNPAID);


        // 7. Lưu booking vào DB
        BookingsEntity saved = bookingRepository.save(booking);

//        eventPublisher.publishEvent(new BookingCreatedEvent(
        BookingCreatedEvent event = new BookingCreatedEvent(
                saved.getId(),
                saved.getBookingCode(),
                user.getEmail(),
                user.getFullName(),
                court.getCourtName(),
                court.getCourtType().name(),
                saved.getBookingDate(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getTotalAmount(),
                customer.getNumberPhone()
        );
        emailService.sendBookingEmail(event, EmailType.PENDING);
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

    @Override
    public BookingDTO getBookingById(Long bookingId, Long userId) {

        BookingsEntity bookingsEntity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("User not found"));

        // Kiểm tra booking có thuộc user hiện tại không
        if (!bookingsEntity.getCustomer().getUsers().getId().equals(userId)) {
            throw new BookingException("You are not allowed to access this booking");
        }
        return BookingMapper.toBookingDTO(bookingsEntity);
    }

    @Override
    public List<BookingDTO> getAllBookingByUserId(Long userId) {
        List<BookingsEntity> bookingsEntityList = bookingRepository.findAllByCustomer_Users_Id(userId);
        return bookingsEntityList
                .stream()
                .map(BookingMapper::toBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO cancelBooking(Long bookingId, Long userId) {
        BookingsEntity booking = bookingRepository.findByIdAndCustomer_Users_Id(bookingId, userId);
        if (booking == null) {
            throw new BookingException("Booking not found or unauthorized");
        }

        // Kiểm tra thời gian
        LocalDateTime bookingDateTime = LocalDateTime.of(
                booking.getBookingDate(),
                booking.getStartTime()
        );
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new BookingException("Cannot cancel past bookings");
        }

        // Kiểm tra trạng thái
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking already cancelled");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BookingException("Cannot cancel a paid booking");
        }

        //Cập nhật trạng thái
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.UNPAID); // giữ nguyên unpaid
        BookingsEntity saved = bookingRepository.save(booking);

        //Gửi email thông báo hủy
        BookingCreatedEvent event = new BookingCreatedEvent(
                saved.getId(),
                saved.getBookingCode(),
                booking.getCustomer().getUsers().getEmail(),
                booking.getCustomer().getUsers().getFullName(),
                booking.getCourt().getCourtName(),
                booking.getCourt().getCourtType().name(),
                saved.getBookingDate(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getTotalAmount(),
                booking.getCustomer().getNumberPhone()
        );
        emailService.sendBookingEmail(event, EmailType.CANCELLED);

        return BookingMapper.toBookingDTO(saved);
    }

    // --- ADMIN ---
    @Override
    public PageResponse<BookingDTO> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by( "bookingDate").descending());

        Page<BookingsEntity> bookings = bookingRepository.findAll(pageable);

        List<BookingDTO> bookingDTOs = bookings.getContent()
                .stream()
                .map(BookingMapper::toBookingDTO)
                .toList();

        return new PageResponse<>(
                bookingDTOs,
                bookings.getNumber(),
                bookings.getSize(),
                bookings.getTotalElements(),
                bookings.getTotalPages(),
                bookings.isFirst(),
                bookings.isLast()
        );
    }

    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus newStatus ) {
        BookingsEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        if (newStatus == BookingStatus.CONFIRMED) {
            eventPublisher.publishEvent(new BookingCreatedEvent(
                    booking.getId(),
                    booking.getBookingCode(),
                    booking.getCustomer().getUsers().getEmail(),
                    booking.getCustomer().getUsers().getFullName(),
                    booking.getCourt().getCourtName(),
                    booking.getCourt().getCourtType().name(),
                    booking.getBookingDate(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    booking.getTotalAmount(),
                    booking.getCustomer().getNumberPhone()
            ));
        }
        return BookingMapper.toBookingDTO(booking);
    }

    @Override
    public void deleteBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new RuntimeException("Booking not found");
        }
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingDTO processPayment(Long bookingId, PaymentMethod method, String adminName) {
        //Lấy booking
        BookingsEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Booking must be confirmed before payment");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BookingException("Booking already paid");
        }

        //Tạo transaction (booking 1-1)
        TransactionEntity tx = new TransactionEntity();

        tx.setTransactionDate(LocalDate.now());
        tx.setPaymentMethod(method);
        tx.setBooking(booking);
        tx.setAmount(booking.getTotalAmount());
        tx.setCreateBy(adminName);
        transactionRepository.save(tx);

        //Cập nhật trạng thái thanh toán của booking
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setTransaction(tx);

        bookingRepository.save(booking);

        return BookingMapper.toBookingDTO(booking);
    }

    @Override
    public BookingDTO getBookingByIdForAdmin(Long bookingId) {
        BookingsEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        return BookingMapper.toBookingDTO(booking);
    }

}
