package com.rental.server.common;

/**
 * 业务异常：用于主动抛出的可预期错误（如"房源不存在"、"无权限"）
 * 会被 GlobalExceptionHandler 捕获并转为统一响应格式
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
