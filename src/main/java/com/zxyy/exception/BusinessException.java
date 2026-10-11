package com.zxyy.exception;

/**
 * 业务规则不满足时主动抛出，由全局异常处理器返回提示信息。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
