package org.badmintonchain.service;

import org.badmintonchain.model.dto.CustomerUserDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.springframework.data.domain.Page;

public interface CustomerService {
    PageResponse<CustomerUserDTO> getAllUsers(int page, int size, String keyword, Boolean isActive);
    PageResponse<CustomerUserDTO> getAllAdmins(int page, int size);
    CustomerUserDTO getUserDetail(Long userId);
    CustomerUserDTO updateUser(Long userId, CustomerUserDTO request, boolean isAdmin);
    void deleteUser(Long userId);
}
