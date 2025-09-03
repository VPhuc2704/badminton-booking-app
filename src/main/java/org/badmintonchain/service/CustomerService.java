package org.badmintonchain.service;

import org.badmintonchain.model.dto.CustomerUserDTO;
import org.springframework.data.domain.Page;

public interface CustomerService {
    Page<CustomerUserDTO> getAllUsers(int page, int size, String keyword, Boolean isActive);

    CustomerUserDTO getUserDetail(Long userId);

    CustomerUserDTO updateUser(Long userId, CustomerUserDTO request);

    void deleteUser(Long userId);
}
