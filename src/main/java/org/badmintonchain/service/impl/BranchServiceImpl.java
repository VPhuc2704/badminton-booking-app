package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.BranchDTO;
import org.badmintonchain.model.dto.requests.BranchWithManagerDTO;
import org.badmintonchain.model.entity.BranchEntity;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.enums.RoleName;
import org.badmintonchain.model.mapper.BranchMapper;
import org.badmintonchain.repository.BranchRepository;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.service.AuthService;
import org.badmintonchain.service.BranchService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BranchDTO updateBranch(Long id, BranchDTO dto) {
        UsersEntity currentUser = authService.getCurrentUser();

        if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới được cập nhật chi nhánh");
        }

        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

        // Cập nhật thông tin cơ bản
        if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
        if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
        if (dto.getPhone() != null) branch.setPhone(dto.getPhone());
        if (dto.getIsActive() != null) branch.setIsActive(dto.getIsActive());

        // === Xử lý người quản lý ===
        if (dto.getManagerId() != null) {
            UsersEntity newManager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + dto.getManagerId()));

            // Kiểm tra vai trò hợp lệ
            if (newManager.getRoleName() != RoleName.STAFF) {
                throw new RuntimeException("Chỉ có nhân viên (STAFF) mới được làm quản lý chi nhánh");
            }

            // Nếu staff này đã manager của chi nhánh khác
            if (branchRepository.existsByManager_Id(newManager.getId())) {
                throw new RuntimeException("Quản lý chi nhánh khác");
            }

            // Nếu có quản lý cũ -> khóa tài khoản
            UsersEntity oldManager = branch.getManager();
            if (oldManager != null && !oldManager.getId().equals(newManager.getId())) {
                oldManager.setActive(false);
                userRepository.save(oldManager);
            }

            // Cập nhật quản lý mới
            newManager.setRoleName(RoleName.STAFF);
            newManager.setActive(true);
            branch.setManager(newManager);

            userRepository.save(newManager);
        }

        BranchEntity saved = branchRepository.save(branch);
        return BranchMapper.toDTO(saved);
    }

    @Override
    public void deleteBranch(Long id) {
        UsersEntity currentUser = authService.getCurrentUser();
        if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới được xoá chi nhánh");
        }

        branchRepository.deleteById(id);
    }

    @Override
    public BranchDTO getBranchById(Long id) {
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));
        return BranchMapper.toDTO(branch);
    }

    @Override
    public List<BranchDTO> getAllBranches() {
        List<BranchEntity> branches = branchRepository.findAll();
        return branches.stream().map(BranchMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public BranchDTO createBranchWithManager(BranchWithManagerDTO dto) {
        UsersEntity currentUser = authService.getCurrentUser();

        if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới được tạo chi nhánh");
        }

        // Kiểm tra trùng email
        if (userRepository.existsByEmail(dto.getManagerEmail())) {
            throw new RuntimeException("Email quản lý đã tồn tại trong hệ thống");
        }

        //Tạo người quản lý (Staff)
        UsersEntity manager = UsersEntity.builder()
                .fullName(dto.getManagerName())
                .email(dto.getManagerEmail())
                .passwordHash(passwordEncoder.encode(
                        dto.getManagerPassword() != null ? dto.getManagerPassword() : "123456"
                ))
                .isActive(true)
                .roleName(RoleName.STAFF)
                .build();

        manager = userRepository.save(manager);

        //Tạo chi nhánh
        BranchEntity branch = BranchEntity.builder()
                .branchName(dto.getBranchName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .isActive(true)
                .manager(manager)
                .build();

        branch = branchRepository.save(branch);

        return BranchMapper.toDTO(branch);
    }
}
