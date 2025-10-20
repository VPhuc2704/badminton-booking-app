package org.badmintonchain.service;

import org.badmintonchain.model.dto.BranchDTO;
import org.badmintonchain.model.dto.requests.BranchWithManagerDTO;

import java.util.List;

public interface BranchService {
    BranchDTO updateBranch(Long id, BranchDTO dto);
    void deleteBranch(Long id);
    BranchDTO getBranchById(Long id);
    List<BranchDTO> getAllBranches();
    BranchDTO createBranchWithManager(BranchWithManagerDTO dto);
}
