package org.badmintonchain.controller;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.dto.BranchDTO;
import org.badmintonchain.model.dto.requests.BranchWithManagerDTO;
import org.badmintonchain.service.BranchService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor

public class BranchController {
    private final BranchService branchService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/create-with-manager")
    public ResponseEntity<ApiResponse<BranchDTO>> createBranchWithManager(@RequestBody BranchWithManagerDTO dto) {
        BranchDTO created = branchService.createBranchWithManager(dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Tạo chi nhánh thành công", 200, created, "/api/branches/create-with-manager")
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(@PathVariable Long id, @RequestBody BranchDTO dto) {
        BranchDTO updated = branchService.updateBranch(id, dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Cập nhật chi nhánh thành công", 200, updated, "/api/branches/" + id)
        );
    }
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Xóa chi nhánh thành công", 200, null, "/api/branches/" + id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(@PathVariable Long id) {
        BranchDTO dto = branchService.getBranchById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy thông tin chi nhánh thành công", 200, dto, "/api/branches/" + id)
        );
    }
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches() {
        List<BranchDTO> list = branchService.getAllBranches();
        return ResponseEntity.ok(
                new ApiResponse<>("Danh sách chi nhánh", 200, list, "/api/branches")
        );
    }

    @PreAuthorize("hasAnyRole('STAFF')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<BranchDTO>> getMyBranch() {
        BranchDTO dto = branchService.getMyBranch();
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy chi nhánh của bạn thành công", 200, dto, "/api/admin/branches/me")
        );
    }

}
