package com.coflxl.api.gateway.exception;

import com.coflxl.api.common.enums.ErrorCodeEnum;
import com.coflxl.api.common.exception.ApiException;
import com.coflxl.api.common.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ApiResponse<Object> handleApiException(ApiException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception e) {
        return ApiResponse.error(ErrorCodeEnum.SYSTEM_ERROR.getCode(), e.getMessage());
    }
}
