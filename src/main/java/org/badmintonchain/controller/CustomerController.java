package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.exceptions.UsersException;
import org.badmintonchain.model.dto.CustomerUserDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.dto.requests.ChangePasswordRequest;
import org.badmintonchain.model.dto.requests.CreateUserRequest;
import org.badmintonchain.model.dto.response.UserInfoDTO;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.security.CustomUserDetails;
import org.badmintonchain.service.CustomerService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/admin/all/users")
    public ResponseEntity<ApiResponse<PageResponse<CustomerUserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            HttpServletRequest request
    ) {
        PageResponse<CustomerUserDTO> result = customerService.getAllUsers(page, size, keyword, isActive);
        return ResponseEntity.ok(
                new ApiResponse<>("Users retrieved successfully", HttpStatus.OK.value(), result, request.getRequestURI())
        );
    }

    // Lấy tất cả admin
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<PageResponse<CustomerUserDTO>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {

        PageResponse<CustomerUserDTO> admins = customerService.getAllAdmins(page, size);

        return ResponseEntity.ok(
                new ApiResponse<>("Get all admins successfully", 200, admins, request.getRequestURI())
        );
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> getUserDetailByAdmin(@PathVariable Long id,
                                                                      @AuthenticationPrincipal CustomUserDetails currentUser,
                                                                      HttpServletRequest request) {
        boolean isAdmin = currentUser.getUser().getRoleName() == RoleName.ADMIN;
        boolean isStaffBranch = currentUser.getUser().getRoleName() == RoleName.STAFF;

        if (!isAdmin && !isStaffBranch) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("Access denied", 403, null, request.getRequestURI()));
        }

        CustomerUserDTO dto = customerService.getUserDetail(id);
        return ResponseEntity.ok(
                new ApiResponse<>("User retrieved successfully", HttpStatus.OK.value(), dto, request.getRequestURI())
        );
    }

    @GetMapping("users/me")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

        Long currentUserId = currentUser.getUser().getId();
        CustomerUserDTO dto = customerService.getUserDetail(currentUserId);

        return ResponseEntity.ok(
                new ApiResponse<>("Profile retrieved successfully", HttpStatus.OK.value(), dto, request.getRequestURI())
        );
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody CustomerUserDTO requestDto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        Long currentUserId = currentUser.getUser().getId();
        boolean isAdmin = currentUser.getUser().getRoleName() == RoleName.ADMIN;
        boolean isStaff = currentUser.getUser().getRoleName() == RoleName.STAFF;
        boolean isSelf = currentUserId.equals(id);

        if (!isAdmin && !isStaff && !isSelf) {
            // user thường mà update người khác -> cấm
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("Access denied", 403, null, request.getRequestURI()));
        }

        CustomerUserDTO updated = customerService.updateUser(id, requestDto, isAdmin, isStaff);

        return ResponseEntity.ok(
                new ApiResponse<>("User updated successfully", HttpStatus.OK.value(), updated, request.getRequestURI())
        );
    }


    @DeleteMapping("/admin/users/{id}")
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
                HttpStatus.NO_CONTENT.value(),
                null,
                request.getRequestURI()
        ));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                              @RequestBody ChangePasswordRequest changePasswordRequest,
                                                              HttpServletRequest request){
            try {
                customerService.changePassword(currentUser.getUser().getId(), changePasswordRequest);
                return  ResponseEntity.ok(new ApiResponse<>(
                        "Cập nhật password thành công",
                        HttpStatus.CREATED.value(),
                        null,
                        request.getRequestURI()
                ));
            } catch (UsersException ex) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                null,
                                request.getRequestURI()
                        )
                );
            }
    }

    @PostMapping("/admin/users")
    public ResponseEntity<ApiResponse<CustomerUserDTO>> createUser(@AuthenticationPrincipal  CustomUserDetails currentUser,
                                                                   @RequestBody CreateUserRequest request,
                                                                   HttpServletRequest httpRequest) {
        if (currentUser.getUser().getRoleName() != RoleName.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("Access denied", 403, null, httpRequest.getRequestURI()));
        }
        try {
            CustomerUserDTO user = customerService.createUser(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Tạo tài khoản thành công",
                    HttpStatus.CREATED.value(),
                    user,
                    httpRequest.getRequestURI()
            ));
        } catch (UsersException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null,
                    httpRequest.getRequestURI()
            ));
        }
    }


}
