package org.badmintonchain.utils;

import lombok.Data;

@Data
public class ApiResponse<T>{
    private String messenger;
    private int status;
    private T detail;
    private String instance;

    public ApiResponse(String messenger, int status, T detail, String instance) {
        this.messenger = messenger;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

}
