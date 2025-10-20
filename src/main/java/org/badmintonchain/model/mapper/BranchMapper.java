package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.BranchDTO;
import org.badmintonchain.model.entity.BranchEntity;

public class BranchMapper {
    public static BranchDTO toDTO(BranchEntity entity) {
        return BranchDTO.builder()
                .id(entity.getId())
                .branchName(entity.getBranchName())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .isActive(entity.getIsActive())
                .managerId(entity.getManager() != null ? entity.getManager().getId() : null)
                .build();
    }

    public static BranchEntity toEntity(BranchDTO dto) {
        return BranchEntity.builder()
                .branchName(dto.getBranchName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
