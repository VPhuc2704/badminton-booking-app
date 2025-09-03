package org.badmintonchain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.badmintonchain.model.entity.BaseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Slf4j
public class CustomerEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/*
    CREATE TABLE customers (
        id SERIAL PRIMARY KEY,
        user_id INTEGER REFERENCES users(id),
        customer_code VARCHAR(20) UNIQUE,
        date_of_birth DATE,
        gender ENUM('male', 'female', 'other'),
        address TEXT,
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
 */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UsersEntity users;

    @Column(name = "phone")
    private String numberPhone;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<BookingsEntity> bookings = new ArrayList<>();

//    @Column(name = "customer_code")
//    private String customerCode;
//
//    @Column(name = "date_of_birth")
//    private Date dateOfBirth;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "gender")
//    private GenderEnum gender;
//
//    @Column(name = "address")
//    private String address;
//
//    @Column(name = "notes")
//    private String notes;
}
