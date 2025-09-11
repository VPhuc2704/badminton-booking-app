package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    COD("Tiền mặt"),
    TRANSFER("Chuyển khoản");

    private final String nameMethod;
    PaymentMethod(String nameMethod) {
        this.nameMethod = nameMethod;
    }
}
