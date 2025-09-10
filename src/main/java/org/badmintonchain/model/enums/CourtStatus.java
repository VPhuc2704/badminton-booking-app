package org.badmintonchain.model.enums;

import lombok.Getter;

//CREATE TYPE court_status AS ENUM ('available', 'maintenance', 'unavailable');
@Getter
public enum CourtStatus {
    AVAILABLE("Có Sẵn"),
    MAINTENANCE("Đang bảo trì"),
    UNAVAILABLE("Không có sẵn");

    private final String statusName;

    CourtStatus(String statusName) {
        this.statusName = statusName;
    }

}
