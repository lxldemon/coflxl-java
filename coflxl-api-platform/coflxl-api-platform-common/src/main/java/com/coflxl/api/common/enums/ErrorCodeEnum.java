package com.coflxl.api.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    SUCCESS(200, "Success"),
    SYSTEM_ERROR(500, "System Error"),
    PARAM_ERROR(400, "Parameter Error"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    API_NOT_FOUND(404, "API Not Found"),
    API_OFFLINE(4041, "API is Offline");

    private final Integer code;
    private final String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
