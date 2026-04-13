package com.coflxl.api.common.exception;

import com.coflxl.api.common.enums.ErrorCodeEnum;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final Integer code;

    public ApiException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ApiException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
