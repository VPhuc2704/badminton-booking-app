package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ADMIN("ADMIN"),
    CUSTOMER("CUSTOMER");
    private final String nameCode;

    RoleName(String nameCode) {
        this.nameCode = nameCode;
    }
}