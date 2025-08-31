package org.badmintonchain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.badmintonchain.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class BookingsEntity extends BaseEntity{
/*
    CREATE TABLE bookings (
        booking_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        booking_code VARCHAR(20) UNIQUE,
        customer_id INT NOT NULL,
        court_id INT NOT NULL,
        booking_date DATE NOT NULL,
        start_time TIME NOT NULL,
        end_time TIME NOT NULL,
        status booking_status DEFAULT 'pending',
        total_amount DECIMAL(10,2),
        payment_status payment_status DEFAULT 'unpaid',
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
        FOREIGN KEY (court_id) REFERENCES courts(court_id)
   );
 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", unique = true)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime  endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status",nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", precision =  10, scale = 2)
    private BigDecimal totalAmount;
//
//    @Column(name = "payment_status", nullable = false)
//    private String paymentStatus;
//
//    @Column(name = "notes", columnDefinition = "TEXT")
//    private String notes;

}
