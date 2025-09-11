package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum CourtType {
    INDOOR("Trong nha"),
    OUTDOOR("Ngoai troi");

    private final String courtType;
    CourtType(String courtType) {
        this.courtType = courtType;
    }
}
