package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.exceptions.CourtException;
import org.badmintonchain.exceptions.UsersException;
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

        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new CourtException("Không tìm thấy chi nhánh"));

        boolean isAdmin = currentUser.getRoleName() == RoleName.ADMIN;
        boolean isStaff = currentUser.getRoleName() == RoleName.STAFF;
        boolean isManagerOfBranch = isStaff
                && branch.getManager() != null
                && branch.getManager().getId().equals(currentUser.getId());

        if (!isAdmin && !isManagerOfBranch) {
            throw new UsersException("Bạn không có quyền chỉnh sửa chi nhánh này");
        }

        if(isAdmin){
            // Cập nhật thông tin cơ bản
            if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
            if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
            if (dto.getPhone() != null) branch.setPhone(dto.getPhone());
            if (dto.getIsActive() != null) branch.setIsActive(dto.getIsActive());
            // === Xử lý người quản lý ===
            if (dto.getManagerId() != null) {
                UsersEntity newManager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new UsersException("Không tìm thấy user với ID: " + dto.getManagerId()));

                // Kiểm tra vai trò hợp lệ
                if (newManager.getRoleName() != RoleName.STAFF) {
                    throw new CourtException("Chỉ có nhân viên (STAFF) mới được làm quản lý chi nhánh");
                }

                // Nếu staff này đã manager của chi nhánh khác
                if (branchRepository.existsByManager_Id(newManager.getId())) {
                    throw new CourtException("Quản lý chi nhánh khác");
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
        } else if (isManagerOfBranch) {
            if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
            if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
            if (dto.getPhone() != null) branch.setPhone(dto.getPhone());
            //Không cho staff chỉnh isActive hay managerId
        }

        BranchEntity saved = branchRepository.save(branch);
        return BranchMapper.toDTO(saved);
    }

    @Override
    public void deleteBranch(Long id) {
        UsersEntity currentUser = authService.getCurrentUser();
        if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new CourtException("Chỉ admin mới được xoá chi nhánh");
        }

        branchRepository.deleteById(id);
    }

    @Override
    public BranchDTO getBranchById(Long id) {
        UsersEntity currentUser = authService.getCurrentUser();

        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new CourtException("Không tìm thấy chi nhánh"));

        if(currentUser.getRoleName() == RoleName.STAFF){
            BranchEntity staffBranch = branchRepository.findByManager_Id(currentUser.getId());
            if (staffBranch == null || !staffBranch.getId().equals(id)) {
                throw new CourtException("Bạn chỉ có thể xem chi nhánh do bạn quản lý");
            }
        } else if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new CourtException("Bạn không có quyền truy cập chi nhánh này");
        }
        return BranchMapper.toDTO(branch);
    }

    @Override
    public List<BranchDTO> getAllBranches() {
        List<BranchEntity> branches = branchRepository.findAll();
        return branches.stream().map(BranchMapper::toDTO).toList();
    }

    @Override
    public BranchDTO getMyBranch() {
        UsersEntity currentUser = authService.getCurrentUser();

        // Chỉ cho phép STAFF
        if (currentUser.getRoleName() != RoleName.STAFF) {
            throw new CourtException("Chỉ nhân viên mới có thể xem chi nhánh của mình");
        }

        // Tìm chi nhánh do staff này quản lý
        BranchEntity branch = branchRepository.findByManager_Id(currentUser.getId());

        if (branch == null) {
            throw new CourtException("Bạn chưa được phân công quản lý chi nhánh nào");
        }

        return BranchMapper.toDTO(branch);
    }


    @Override
    @Transactional
    public BranchDTO createBranchWithManager(BranchWithManagerDTO dto) {
        UsersEntity currentUser = authService.getCurrentUser();

        if (currentUser.getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới được tạo chi nhánh");
        }

        // Tạo chi nhánh trước để có ID tự sinh
        BranchEntity branch = BranchEntity.builder()
                .branchName(dto.getBranchName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .isActive(true)
                .build();

        branch = branchRepository.save(branch);

        //Sinh mã CN dựa theo id (vd: CN01, CN02...)
        String branchCode = String.format("CN%02d", branch.getId());

        // Sinh tên và email mặc định cho staff
        String baseName = "Staff " + branchCode.substring(2); // Staff 01
        String baseEmail = "Staff" + branchCode + "@gmail.com"; // StaffCN01@gmail.com

        //Nếu trùng email → thêm hậu tố _1, _2,...
        String finalEmail = baseEmail;
        int counter = 1;
        while (userRepository.existsByEmail(finalEmail)) {
            finalEmail = "Staff" + branchCode + "_" + counter + "@gmail.com";
            counter++;
        }

        // Tạo user manager
        UsersEntity manager = UsersEntity.builder()
                .fullName(baseName)
                .email(finalEmail)
                .passwordHash(passwordEncoder.encode(
                        dto.getManagerPassword() != null ? dto.getManagerPassword() : "123456"
                ))
                .isActive(true)
                .roleName(RoleName.STAFF)
                .build();

        manager = userRepository.save(manager);

        // Gán staff vào chi nhánh
        branch.setManager(manager);
        BranchEntity saved = branchRepository.save(branch);

        return BranchMapper.toDTO(saved);
    }

}
