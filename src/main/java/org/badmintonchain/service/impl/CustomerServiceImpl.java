package org.badmintonchain.service.impl;

import org.badmintonchain.exceptions.UsersException;
import org.badmintonchain.model.dto.CustomerUserDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.dto.requests.ChangePasswordRequest;
import org.badmintonchain.model.dto.requests.CreateUserRequest;
import org.badmintonchain.model.dto.response.UserInfoDTO;
import org.badmintonchain.model.entity.BookingsEntity;
import org.badmintonchain.model.entity.BranchEntity;
import org.badmintonchain.model.entity.CustomerEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.repository.BookingRepository;
import org.badmintonchain.repository.BranchRepository;
import org.badmintonchain.repository.CustomerRepository;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.CustomerService;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  CustomerRepository customerRepository;
    @Autowired
    private BookingRepository  bookingRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService  authService;
    @Autowired
    private BranchRepository branchRepository;

    @Override
    public PageResponse<CustomerUserDTO> getAllUsers(int page, int size, String keyword, Boolean isActive) {
        UsersEntity currentUser = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

//        Page<UsersEntity> users = userRepository.findAll(pageable);

        Page<UsersEntity> users;
//                = userRepository.findAllCustomers(RoleName.CUSTOMER, keyword != null ?  keyword : "" , isActive, pageable);

        if (currentUser.getRoleName() == RoleName.ADMIN) {
            // ADMIN: xem toàn bộ khách hàng
            users = userRepository.findAllCustomers(
                    RoleName.CUSTOMER,
                    keyword != null ? keyword : "",
                    isActive,
                    pageable
            );
        } else if (currentUser.getRoleName() == RoleName.STAFF) {
            // STAFF: chỉ xem khách hàng thuộc chi nhánh của mình
            Long branchId = currentUser.getBranch().getId();

            users = userRepository.findAllCustomersByBranch(
                    RoleName.CUSTOMER,
                    branchId,
                    keyword != null ? keyword : "",
                    isActive,
                    pageable
            );
        } else {
            throw new UsersException("Bạn không có quyền xem danh sách khách hàng");
        }

        Page<CustomerUserDTO> dtoPage = users.map(this::toDTO);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
    }

    public PageResponse<CustomerUserDTO> getAllAdmins(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<UsersEntity> users = userRepository.findAllByRoleName(RoleName.ADMIN, pageable);

        Page<CustomerUserDTO> dtoPage = users.map(this::toDTO);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
    }

    @Override
    public CustomerUserDTO getUserDetail(Long userId) {
        UsersEntity currentUser = authService.getCurrentUser();

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
//            if (request.getEmail() != null) {
//                user.setEmail(request.getEmail());
//            }
        }

        // --- ADMIN có thể update tất cả ---
        else {
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
//            if (request.getEmail() != null) {
//                user.setEmail(request.getEmail());
//            }
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

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UsersEntity users =  userRepository.findById(userId)
                .orElseThrow(() -> new UsersException("User not found"));

        if(!passwordEncoder.matches(request.getOldPassword(), users.getPasswordHash())){
            throw new UsersException("Mật khẩu hiện tại không đúng");
        }

        users.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(users);
    }

    @Override
    public CustomerUserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UsersException("Email đã tồn tại");
        }

        // 👉 Đặt mật khẩu mặc định
        String defaultPassword = "123456";

        UsersEntity user = new UsersEntity();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setRoleName(request.getRole() != null ? request.getRole() : RoleName.CUSTOMER);
        user.setActive(true);

        UsersEntity savedUser = userRepository.save(user);

        CustomerEntity customer = new CustomerEntity();
        customer.setUsers(user);
        customer.setNumberPhone(request.getNumberPhone());
        customerRepository.save(customer);

        return toDTO(savedUser);
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
