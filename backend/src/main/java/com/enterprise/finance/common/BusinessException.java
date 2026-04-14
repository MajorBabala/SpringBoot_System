package com.enterprise.finance.common;

/**
 * 业务异常：由统一异常处理器转为 ApiResponse。
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BusinessException of(String message) {
        return new BusinessException(400, message);
    }
}

