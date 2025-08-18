package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ADMIN("ADMIN"),
    CUSTOMMER("CUSTOMER");
    private final String nameCode;

    RoleName(String nameCode) {
        this.nameCode = nameCode;
    }
}