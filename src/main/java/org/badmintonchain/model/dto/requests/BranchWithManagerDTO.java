package org.badmintonchain.model.dto.requests;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchWithManagerDTO {
    // Thông tin chi nhánh
    private String branchName;
    private String address;
    private String phone;

    // Thông tin người quản lý
    private String managerName;
    private String managerEmail;
    private String managerPassword; // hoặc generate random
}
