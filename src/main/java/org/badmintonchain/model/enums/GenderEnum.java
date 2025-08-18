package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE("MALE"),
    FEMALE("FEMALE"),
    ORTHER("Other");

    private final String genderName;
    GenderEnum (String genderName) {
        this.genderName = genderName;
    }

}
