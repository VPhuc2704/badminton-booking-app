package org.badmintonchain.model.enums;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Chờ xữ lý"),
    CONFIRMED("Xác Nhận"),
    CANCELLED("Đã Hủy");

    private final String bookingStatus;
    BookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
