package org.badmintonchain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchPublic {
    private Long id;
    private String branchName;
    private String address;
    private String phone;
}
