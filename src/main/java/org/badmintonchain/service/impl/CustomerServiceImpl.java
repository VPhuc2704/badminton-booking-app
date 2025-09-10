package org.badmintonchain.service.impl;

import org.badmintonchain.exceptions.UsersException;
import org.badmintonchain.model.dto.CustomerUserDTO;
import org.badmintonchain.model.entity.CustomerEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.CustomerRepository;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  CustomerRepository customerRepository;
    @Autowired
    private BookingRepository  bookingRepository;

    @Override
    public Page<CustomerUserDTO> getAllUsers(int page, int size, String keyword, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<UsersEntity> users = userRepository.findAll(pageable);

        return users.map(this::toDTO);
    }

    @Override
    public CustomerUserDTO getUserDetail(Long userId) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsersException("User not found"));
        return toDTO(user);
    }

    @Override
    @Transactional
    public CustomerUserDTO updateUser(Long userId, CustomerUserDTO request, boolean isAdmin) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsersException("User not found"));

        // --- USER chỉ được update 1 số trường ---
        if (!isAdmin) {
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
        }

        // --- ADMIN có thể update tất cả ---
        else {
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getActive() != null) {
                user.setActive(request.getActive());
            }
            if (request.getRoleName() != null) {
                user.setRoleName(RoleName.valueOf(request.getRoleName()));
            }
        }

        userRepository.save(user);

        // --- Nếu là CUSTOMER thì quản lý thêm thông tin khách hàng ---
        if (user.getRoleName() == RoleName.CUSTOMER) {
            CustomerEntity customer = customerRepository.findByUsers_Id(userId)
                    .orElseGet(() -> {
                        CustomerEntity c = new CustomerEntity();
                        c.setUsers(user);
                        return c;
                    });

            // số điện thoại: cả User và Admin đều có thể sửa
            if (request.getNumberPhone() != null) {
                customer.setNumberPhone(request.getNumberPhone());
            }
            customerRepository.save(customer);
        }

        return toDTO(user);
    }


    @Override
    public void deleteUser(Long userId) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsersException("User not found"));

        if (user.getRoleName() == RoleName.CUSTOMER) {
            Optional<CustomerEntity> customerOpt = customerRepository.findByUsers_Id(userId);

            if (customerOpt.isPresent()) {
                CustomerEntity customer = customerOpt.get();
                boolean hasBooking = bookingRepository.existsByCustomer_Id(customer.getId());

                if (hasBooking) {
                    // Soft delete nếu có booking
                    user.setActive(false);
                    userRepository.save(user);
                    return;
                } else {
                    // Hard delete nếu chưa booking
                    customerRepository.delete(customer);
                    userRepository.delete(user);
                    return;
                }
            }
        }

        // Admin / Staff thì hard delete
        userRepository.delete(user);
    }


    private CustomerUserDTO toDTO(UsersEntity user) {
        CustomerUserDTO dto = new CustomerUserDTO();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleName(user.getRoleName().name());
        dto.setActive(user.isActive());

        customerRepository.findByUsers_Id(user.getId())
                .ifPresent(customer -> {
                    dto.setCustomerId(customer.getId());
                    dto.setNumberPhone(customer.getNumberPhone());
                });

        return dto;
    }
}
