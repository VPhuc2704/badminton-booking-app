package org.badmintonchain.model.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.badmintonchain.model.enums.CourtStatus;
import org.badmintonchain.model.enums.CourtType;

import java.math.BigDecimal;
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
    private List<String> images;
    private Boolean isActive;
    private CourtStatus status;
}
