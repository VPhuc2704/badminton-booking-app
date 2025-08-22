package org.badmintonchain.model.enums;

import lombok.Getter;

//CREATE TYPE court_status AS ENUM ('available', 'maintenance', 'unavailable');
@Getter
public enum CourtStatus {
    available("Có Sẵn"),
    maintenance("Đang bảo trì"),
    unavailable("Không có sẵn");

    private final String statusName;

    CourtStatus(String statusName) {
        this.statusName = statusName;
    }

}
