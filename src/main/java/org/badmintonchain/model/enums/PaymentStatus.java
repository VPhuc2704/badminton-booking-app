package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID("chưa thanh toán"),
    PAID("đã thanh toán");

    private final String nameStatus;
    PaymentStatus(String nameStatus) {
        this.nameStatus = nameStatus;
    }
}
