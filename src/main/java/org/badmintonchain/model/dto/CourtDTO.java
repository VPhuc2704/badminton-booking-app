package org.badmintonchain.model.dto;
import lombok.*;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.model.enums.CourtType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourtDTO {
    private Long id;
    private String courtName;
    private CourtType courtType;
    private BigDecimal hourlyRate;
    private String description;
    private List<String> images = new ArrayList<>();
    private Boolean isActive;
    private CourtStatus status;
}
