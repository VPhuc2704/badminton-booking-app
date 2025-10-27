package org.badmintonchain.service;

import org.badmintonchain.model.dto.BranchDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.model.dto.requests.BranchWithManagerDTO;

public interface BranchService {
    BranchDTO updateBranch(Long id, BranchDTO dto);
    void deleteBranch(Long id);
    BranchDTO getBranchById(Long id);
    PageResponse<BranchDTO> getAllBranches(int page, int size);
    BranchDTO getMyBranch();
    BranchDTO createBranchWithManager(BranchWithManagerDTO dto);
}
