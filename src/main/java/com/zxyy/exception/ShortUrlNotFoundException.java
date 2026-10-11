package com.zxyy.exception;

/** 短码不存在、已删除或已禁用时，访问接口返回 HTTP 404。 */
public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException() {
        super("短链不存在或已失效");
    }
}
