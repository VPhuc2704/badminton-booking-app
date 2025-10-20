package org.badmintonchain.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private Long id;
    private String branchName;
    private String address;
    private String phone;
    private Boolean isActive;
    private Long managerId;

}
