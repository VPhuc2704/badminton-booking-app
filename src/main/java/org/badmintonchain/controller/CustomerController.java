package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.CustomerUserDTO;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.CustomerService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<Page<CustomerUserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            HttpServletRequest request
    ) {
        Page<CustomerUserDTO> result = customerService.getAllUsers(page, size, keyword, isActive);
        return ResponseEntity.ok(
                new ApiResponse<>("Users retrieved successfully", HttpStatus.OK.value(), result, request.getRequestURI())
        );
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> getUserDetail(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        CustomerUserDTO dto = customerService.getUserDetail(id);
        return ResponseEntity.ok(
                new ApiResponse<>("User retrieved successfully", HttpStatus.OK.value(), dto, request.getRequestURI())
        );
    }

    @PutMapping("/user/infor/{id}")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody CustomerUserDTO requestDto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        if (currentUser.getUser().getRoleName() != RoleName.ADMIN &&
                !currentUser.getUser().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("Access denied", 403, null, request.getRequestURI()));
        }

        CustomerUserDTO dto = customerService.updateUser(id, requestDto);
        return ResponseEntity.ok(
                new ApiResponse<>("User updated successfully", HttpStatus.OK.value(), dto, request.getRequestURI())
        );
    }


    @DeleteMapping("/admin/user/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

        // chỉ admin mới được quyền
        if (currentUser.getUser().getRoleName() != RoleName.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("Access denied", 403, null, request.getRequestURI()));
        }

        customerService.deleteUser(id);

        return ResponseEntity.ok(new ApiResponse<>(
                "User deleted successfully",
                HttpStatus.OK.value(),
                null,
                request.getRequestURI()
        ));
    }
}
